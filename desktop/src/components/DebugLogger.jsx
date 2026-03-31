import { useEffect } from "react";

/**
 * DebugLogger - Advanced UI Diagnostic Tool (Deep Logging)
 *
 * Intercepts interactions and analyzes the stacking context to identify
 * elements that block clicks or z-index issues.
 */
export default function DebugLogger({ contextName }) {
    
    useEffect(() => {
        console.group(`[UI-DEBUG] Mount: ${contextName}`);
        console.log("Body Classes:", document.body.className);
        console.log("Body Style:", document.body.getAttribute("style"));
        console.log("Active Modals:", document.querySelectorAll(".modal.show").length);
        console.log("Backdrops:", document.querySelectorAll(".modal-backdrop").length);
        console.log("Active Element (Focus):", document.activeElement);
        console.groupEnd();

        return () => {
            console.log(`[UI-DEBUG] Unmount: ${contextName}`);
        };
    }, [contextName]);

    useEffect(() => {
        const logEvent = (e) => {
            if (e.type === 'mousemove' || e.type === 'pointermove') return;
            
            const target = e.target;
            const isInput = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA';
            
            if (e.defaultPrevented) {
                console.warn(`[UI-DEBUG] 🚫 Event ${e.type} PREVENTED on`, target);
            }

            if (e.type === 'mousedown' || e.type === 'click') {
                console.groupCollapsed(`[UI-DEBUG] ${e.type} @ ${e.clientX},${e.clientY}`);
                console.log("Target:", target);
                
                // CSS Computed analysis
                const style = window.getComputedStyle(target);
                console.log("Computed Style:", {
                    pointerEvents: style.pointerEvents,
                    userSelect: style.userSelect,
                    zIndex: style.zIndex,
                    position: style.position,
                    cursor: style.cursor
                });

                // Check inert
                if (target.closest('[inert]')) {
                    console.error("🚨 ELEMENT IS INERT (blocked by parent)!");
                }

                // Check active element before click
                console.log("Active Element (Before):", document.activeElement);

                if (isInput) {
                    setTimeout(() => {
                        console.log("Active Element (After):", document.activeElement);
                        
                        // Check post-click style (did it change?)
                        const newStyle = window.getComputedStyle(target);
                        if (newStyle.userSelect !== style.userSelect) {
                            console.log(`[UI-DEBUG] 🔄 Style Changed! userSelect: ${style.userSelect} -> ${newStyle.userSelect}`);
                        }

                        if (document.activeElement !== target) {
                            console.error(`[UI-DEBUG] ❌ Focus FAILED on input.`);
                        } else {
                            console.log(`[UI-DEBUG] ✅ Focus SUCCESS (hasFocus: ${document.hasFocus()})`);
                        }
                    }, 100);
                }
                console.groupEnd();
            }
        };

        ['mousedown', 'mouseup', 'click', 'focusin', 'focusout'].forEach(evt => {
            window.addEventListener(evt, logEvent, true); // Capture phase
        });

        return () => {
            ['mousedown', 'mouseup', 'click', 'focusin', 'focusout'].forEach(evt => {
                window.removeEventListener(evt, logEvent, true);
            });
        };
    }, []);

    return null;
}
