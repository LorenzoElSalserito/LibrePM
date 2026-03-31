import { useEffect, useRef, useCallback } from "react";
import { createPortal } from "react-dom";

/**
 * PortalModal - Wrapper for modals using React Portal
 *
 * Version 14.1 (Fix Focus Stealing):
 * - Separated useEffect for event handling and initial focus.
 * - Focus is set only on first mount, not on every re-render.
 * - Used a ref for the onClick callback to avoid adding it to the dependencies.
 *
 * @author Lorenzo DM
 * @since 0.6.13
 * @updated 0.6.15
 */
export default function PortalModal({ children, className = "", style = {}, onClick }) {
    const mount = document.body;
    const modalRef = useRef(null);
    const onClickRef = useRef(onClick);

    // Keep the ref always up to date with the latest callback
    useEffect(() => {
        onClickRef.current = onClick;
    }, [onClick]);

    // Effect for event handling (ESC and backdrop)
    useEffect(() => {
        const handleEsc = (e) => {
            if (e.key === "Escape" && onClickRef.current) {
                onClickRef.current();
            }
        };

        document.addEventListener("keydown", handleEsc);
        return () => document.removeEventListener("keydown", handleEsc);
    }, []); // Executed only once

    // Effect for modal setup/teardown (style and focus)
    useEffect(() => {
        const originalOverflow = mount.style.overflow;
        mount.style.overflow = "hidden";
        mount.classList.add("modal-open");

        // Force reflow for transitions
        if (modalRef.current) {
            void modalRef.current.offsetHeight;
        }

        // Set focus only on initial mount
        const timer = setTimeout(() => {
            if (modalRef.current) {
                const autoFocusEl = modalRef.current.querySelector('[autofocus], [data-autofocus="true"]');
                const firstFocusableEl = modalRef.current.querySelector('input:not([type=hidden]), textarea, select, button');
                
                const targetEl = autoFocusEl || firstFocusableEl;

                if (targetEl) {
                    try {
                        targetEl.focus({ preventScroll: true });
                    } catch (e) {
                        try { targetEl.focus(); } catch (e2) { /* ignore */ }
                    }
                }
            }
        }, 100);

        return () => {
            clearTimeout(timer);
            mount.style.overflow = originalOverflow;
            mount.classList.remove("modal-open");
            // The backdrop is managed by the component itself, no need to remove it globally
        };
    }, [mount]); // Executed only on mount/unmount

    const handleBackdropClick = useCallback((e) => {
        if (e.target === e.currentTarget && onClickRef.current) {
            onClickRef.current();
        }
    }, []);

    const handleDialogClick = useCallback((e) => {
        e.stopPropagation();
    }, []);

    return createPortal(
        <div
            className="modal fade show d-block"
            style={{ backgroundColor: "rgba(0,0,0,0.5)", zIndex: 10000 }}
            onClick={handleBackdropClick}
            data-portal-modal="true"
        >
            <div
                ref={modalRef}
                className={`modal-dialog ${className}`}
                style={style}
                onClick={handleDialogClick}
            >
                <div
                    className="modal-content"
                    style={{
                        maxHeight: "calc(100vh - 3.5rem)",
                        overflowY: "auto"
                    }}
                >
                    {children}
                </div>
            </div>
        </div>,
        mount
    );
}
