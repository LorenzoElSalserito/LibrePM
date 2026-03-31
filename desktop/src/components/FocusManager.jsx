import { useEffect, useRef } from "react";

/**
 * FocusManager - Global focus management for text-boxes in Electron
 *
 * Version 3.2 - Fix webContents key focus on Linux
 *
 * PROBLEM SOLVED:
 * On Linux, after Alt+Tab or actions like "delete note", it can happen that:
 * - window.isFocused() = true (the window has focus at the window manager level)
 * - webContents does NOT have the internal "key focus"
 * - Result: the keyboard does not work even though the window is in the foreground!
 *
 * SOLUTION v3.2:
 * - When you click on a text-box, verify AFTER 100ms whether focus has arrived
 * - If it has NOT arrived, call the new IPC handler `lpm:ensure-webcontent-focus`
 *   which forces webContents.focus() to recover key focus
 * - THEN apply focus to the text-box
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.8.3 - Fix webContents key focus with new IPC handler
 */

// Selector for text-boxes (excludes buttons, checkboxes, radios, file inputs)
const TEXTBOX_SELECTOR = [
  "textarea",
  "input:not([type=button]):not([type=submit]):not([type=reset]):not([type=checkbox]):not([type=radio]):not([type=file])",
  '[contenteditable="true"]',
].join(",");

/**
 * Checks whether an element is disabled or read-only
 */
function isDisabledLike(el) {
  return !!(el?.disabled || el?.readOnly);
}

/**
 * Checks whether an element is a text-box
 */
function isTextBox(el) {
  return !!el?.matches?.(TEXTBOX_SELECTOR);
}

/**
 * Places the caret at the end of the text in an element
 * Works with input, textarea and contenteditable
 */
function placeCaretAtEnd(el) {
  if (!el || !el.isConnected || isDisabledLike(el)) return false;

  // Focus with preventScroll to avoid unwanted scrolling
  try {
    el.focus?.({ preventScroll: true });
  } catch (e) {
    try { el.focus?.(); } catch (e2) { return false; }
  }

  // Place caret for input/textarea
  if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) {
    const len = (el.value ?? "").length;
    try {
      el.setSelectionRange(len, len);
    } catch (e) {
      // Some input types (e.g. date) do not support setSelectionRange
    }
    return true;
  }

  // Place caret for contenteditable
  if (el?.isContentEditable) {
    try {
      const range = document.createRange();
      range.selectNodeContents(el);
      range.collapse(false);
      const sel = window.getSelection();
      sel?.removeAllRanges();
      sel?.addRange(range);
    } catch (e) {
      // Fallback
    }
    return true;
  }

  return true;
}

// Throttling for window focus requests
let _lastWindowFocusRequestAt = 0;
const WINDOW_FOCUS_THROTTLE_MS = 500;

/**
 * Requests window key-focus synchronously
 * Call ONLY if !document.hasFocus()
 */
function requestWindowKeyFocusSync(reason = 'textbox') {
  const now = Date.now();

  // Throttling
  if (now - _lastWindowFocusRequestAt < WINDOW_FOCUS_THROTTLE_MS) {
    return;
  }
  _lastWindowFocusRequestAt = now;

  // If the window is already focused, do nothing
  if (document.hasFocus()) {
    return;
  }

  console.log(`[FocusManager] Requesting window focus (${reason})`);

  try {
    window.LPMWindow?.forceFocusSync?.(reason);
  } catch (e) {
    try { window.LPMWindow?.forceFocus?.(reason); } catch (e2) { /* ignore */ }
  }

  try { window.focus?.(); } catch (e) { /* ignore */ }

  // If after forceFocusSync the document still does not have focus,
  // the problem is webContents key focus — use ensureWebContentFocus
  if (!document.hasFocus()) {
    ensureWebContentFocus(`${reason}-fallback`);
  }
}

/**
 * NEW in v3.2: Forces webContents.focus() to recover key focus
 *
 * This is the fix for the case where:
 * - The window is focused (document.hasFocus() = true)
 * - BUT webContents does not have the key focus
 * - Therefore the keyboard does not work
 */
function ensureWebContentFocus(reason = 'ensure') {
  console.log(`[FocusManager] Ensuring webContent focus (${reason})`);

  try {
    // Use the new IPC handler that ALWAYS calls webContents.focus()
    window.LPMWindow?.ensureWebContentFocusSync?.(reason);
  } catch (e) {
    console.warn('[FocusManager] ensureWebContentFocusSync failed:', e);
    // Fallback: try with forceFocusSync
    try {
      window.LPMWindow?.forceFocusSync?.(reason);
    } catch (e2) { /* ignore */ }
  }
}

export default function FocusManager() {
  // Ref for the text-box the user is trying to use
  const pendingElRef = useRef(null);
  // Ref for the last text-box that had focus
  const lastEditorRef = useRef(null);
  // Timestamp of the last click on a text-box
  const lastTextboxClickRef = useRef(0);
  // Flag to avoid multiple calls
  const isProcessingRef = useRef(false);

  useEffect(() => {
    /**
     * Checks whether focus has arrived on the target text-box
     * If not, helps give it focus - with FIX for webContents key focus
     */
    const ensureFocusOnTextbox = (targetEl, delay = 100) => {
      if (!targetEl || !targetEl.isConnected || isDisabledLike(targetEl)) return;
      if (isProcessingRef.current) return;

      isProcessingRef.current = true;

      setTimeout(() => {
        isProcessingRef.current = false;

        // If the element is no longer in the DOM, look for alternatives
        if (!targetEl.isConnected) {
          const modal = document.querySelector(".modal.show");
          const fallback = modal?.querySelector(TEXTBOX_SELECTOR) ||
              document.querySelector(`${TEXTBOX_SELECTOR}:focus`);
          if (fallback && fallback.isConnected && !isDisabledLike(fallback)) {
            placeCaretAtEnd(fallback);
          }
          return;
        }

        // If focus is already on the target element, all good
        if (document.activeElement === targetEl) {
          return;
        }

        // If focus is on ANOTHER text-box, the user has probably clicked elsewhere
        // Do not interfere
        if (isTextBox(document.activeElement)) {
          return;
        }

        // Focus did not arrive - recovery
        console.log('[FocusManager] Focus assist needed - focus did not arrive on textbox');

        // Step 1: Try direct focus (no IPC)
        placeCaretAtEnd(targetEl);

        // Step 2: Check if it worked after one tick
        setTimeout(() => {
          if (!targetEl.isConnected || isDisabledLike(targetEl)) return;
          if (document.activeElement === targetEl) return; // OK

          // Step 3: ONLY if direct focus fails AND the window is not focused,
          // use IPC as a last resort
          if (!document.hasFocus()) {
            ensureWebContentFocus('focus-assist');
            setTimeout(() => {
              if (targetEl.isConnected && !isDisabledLike(targetEl)) {
                placeCaretAtEnd(targetEl);
              }
            }, 30);
          } else {
            // Window focused but placeCaretAtEnd failed — retry once
            placeCaretAtEnd(targetEl);
          }
        }, 16); // 1 frame

      }, delay);
    };

    /**
     * Schedules focus for when the window becomes key-focused
     */
    const scheduleFocusWhenReady = (el) => {
      pendingElRef.current = el;

      const attempt = (tries = 0) => {
        const target = pendingElRef.current;
        if (!target || !target.isConnected) return;

        if (document.hasFocus()) {
          placeCaretAtEnd(target);
          pendingElRef.current = null;
          return;
        }

        if (tries < 12) {
          requestAnimationFrame(() => attempt(tries + 1));
        }
      };

      requestAnimationFrame(() => attempt(0));
    };

    // ========================================
    // RULE 1: Intercept clicks on text-boxes
    // ========================================
    const onPointerDownCapture = (e) => {
      const raw = e.target;
      if (!raw?.closest) return;

      const el = raw.closest(TEXTBOX_SELECTOR);

      if (el && el.isConnected && !isDisabledLike(el)) {
        lastEditorRef.current = el;
        lastTextboxClickRef.current = Date.now();

        // Light hint for the window manager — NO destructive IPC
        // A pointerdown PROVES the user clicked inside the window,
        // the WM is already giving focus. webContents.focus() via IPC
        // here would be premature and would reset activeElement to body.
        if (!document.hasFocus()) {
          try { window.focus?.(); } catch (_e) { /* ignore */ }
        }

        // Safety net: check after delay whether focus has arrived
        ensureFocusOnTextbox(el, 120);
        return;
      }
    };

    // ========================================
    // RULE 2: Proactive recovery when the window becomes active again
    //
    // On Linux/Electron, after alt-tab or blur/focus cycle, webContents
    // can lose the internal "key focus" even if document.activeElement
    // is correct. The keyboard stops working.
    // Fix: call webContents.focus() to recover key focus,
    // then re-apply DOM focus on the element that had it.
    // ========================================
    let _lastWindowFocusAt = 0;
    const WINDOW_FOCUS_DEBOUNCE_MS = 300;

    const onWindowFocus = () => {
        const now = Date.now();
        if (now - _lastWindowFocusAt < WINDOW_FOCUS_DEBOUNCE_MS) return;
        _lastWindowFocusAt = now;

        const activeEl = document.activeElement;

        // If the document already has focus and there is an active element (not body),
        // everything works — do not interfere
        if (document.hasFocus() && activeEl && activeEl !== document.body) {
            return;
        }

        const targetEl = (activeEl && isTextBox(activeEl))
            ? activeEl
            : (pendingElRef.current || lastEditorRef.current);

        ensureWebContentFocus('window-refocus');

        if (targetEl && targetEl.isConnected && !isDisabledLike(targetEl)) {
            setTimeout(() => placeCaretAtEnd(targetEl), 20);
        }

        pendingElRef.current = null;
    };

    // ========================================
    // RULE 3: Auto-focus on modal open
    // ========================================
    let lastModalSeen = null;
    let modalCheckTimeout = null;

    const mutationObserver = new MutationObserver(() => {
      const modal =
          document.querySelector(".modal.show") ||
          document.querySelector("[data-portal-modal='true']") ||
          document.querySelector("[role='dialog'][data-open='true']");

      if (!modal) {
        lastModalSeen = null;
        return;
      }

      if (modal === lastModalSeen) return;
      lastModalSeen = modal;

      // Cancel previous timeout
      if (modalCheckTimeout) clearTimeout(modalCheckTimeout);

      // Delay to give the modal time to stabilize
      modalCheckTimeout = setTimeout(() => {
        const autoFocusEl =
            modal.querySelector("[data-autofocus='true']") ||
            modal.querySelector("[autofocus]") ||
            modal.querySelector(TEXTBOX_SELECTOR);

        if (autoFocusEl && autoFocusEl.isConnected && !isDisabledLike(autoFocusEl)) {
          if (!document.hasFocus()) {
            requestWindowKeyFocusSync('modal-open');
          }

          // Check after a short while whether focus has arrived
          ensureFocusOnTextbox(autoFocusEl, 100);
        }
      }, 50);
    });

    // ========================================
    // RULE 4: Recover focus after actions that lose it
    // ========================================
    const onFocusOut = (e) => {
      // If focus is leaving a text-box
      if (!isTextBox(e.target)) return;

      // And is not going to another text-box
      const relatedTarget = e.relatedTarget;
      if (relatedTarget && isTextBox(relatedTarget)) return;

      // Save the last used text-box
      lastEditorRef.current = e.target;
    };

    // ========================================
    // SETUP LISTENERS
    // ========================================
    document.addEventListener("pointerdown", onPointerDownCapture, { capture: true });
    document.addEventListener("focusout", onFocusOut, { capture: true });
    window.addEventListener("focus", onWindowFocus, false);

    // Listen for focus event from the main process (more reliable on Linux)
    const unsubAppFocus = window.librepm?.on?.('app:focus', onWindowFocus);

    mutationObserver.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["class", "data-open", "aria-hidden"]
    });

    // ========================================
    // CLEANUP
    // ========================================
    return () => {
      document.removeEventListener("pointerdown", onPointerDownCapture, { capture: true });
      document.removeEventListener("focusout", onFocusOut, { capture: true });
      window.removeEventListener("focus", onWindowFocus, false);
      if (unsubAppFocus) unsubAppFocus();
      mutationObserver.disconnect();
      if (modalCheckTimeout) clearTimeout(modalCheckTimeout);
    };
  }, []);

  return null;
}
