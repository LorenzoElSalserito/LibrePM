/**
 * Locale-aware formatting utilities for LibrePM.
 * Uses Intl APIs for proper localization.
 */

/**
 * Format a date according to locale preferences.
 * @param {string|Date} date - Date string or Date object
 * @param {string} locale - BCP 47 locale (e.g., 'en', 'it')
 * @param {object} options - Intl.DateTimeFormat options
 * @returns {string} Formatted date string
 */
export function formatDate(date, locale = 'en', options = {}) {
    if (!date) return '-';
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '-';
    const defaults = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Intl.DateTimeFormat(locale, { ...defaults, ...options }).format(d);
}

/**
 * Format a date with time.
 * @param {string|Date} date
 * @param {string} locale
 * @returns {string}
 */
export function formatDateTime(date, locale = 'en') {
    return formatDate(date, locale, { hour: '2-digit', minute: '2-digit' });
}

/**
 * Format a currency amount.
 * @param {number} amount
 * @param {string} currency - ISO 4217 code (e.g., 'EUR', 'USD')
 * @param {string} locale
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount, currency = 'EUR', locale = 'en') {
    if (amount == null || isNaN(amount)) return '-';
    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency,
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
    }).format(amount);
}

/**
 * Format a number with locale-appropriate separators.
 * @param {number} number
 * @param {string} locale
 * @param {object} options - Intl.NumberFormat options
 * @returns {string}
 */
export function formatNumber(number, locale = 'en', options = {}) {
    if (number == null || isNaN(number)) return '-';
    return new Intl.NumberFormat(locale, options).format(number);
}

/**
 * Format a percentage.
 * @param {number} value - Value between 0 and 1 (or 0-100 if already percentage)
 * @param {string} locale
 * @param {boolean} isRatio - If true, value is 0-1; if false, value is 0-100
 * @returns {string}
 */
export function formatPercent(value, locale = 'en', isRatio = false) {
    if (value == null || isNaN(value)) return '-';
    const ratio = isRatio ? value : value / 100;
    return new Intl.NumberFormat(locale, {
        style: 'percent',
        minimumFractionDigits: 0,
        maximumFractionDigits: 1,
    }).format(ratio);
}

/**
 * Format a relative time (e.g., "2 days ago").
 * @param {string|Date} date
 * @param {string} locale
 * @returns {string}
 */
export function formatRelativeTime(date, locale = 'en') {
    if (!date) return '-';
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '-';
    const now = Date.now();
    const diff = d.getTime() - now;
    const seconds = Math.round(diff / 1000);
    const minutes = Math.round(seconds / 60);
    const hours = Math.round(minutes / 60);
    const days = Math.round(hours / 24);

    const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
    if (Math.abs(days) >= 1) return rtf.format(days, 'day');
    if (Math.abs(hours) >= 1) return rtf.format(hours, 'hour');
    if (Math.abs(minutes) >= 1) return rtf.format(minutes, 'minute');
    return rtf.format(seconds, 'second');
}
