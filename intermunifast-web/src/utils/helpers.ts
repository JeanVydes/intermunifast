/**
 * Format a date string to localized date and time
 * @param dateString - ISO date string
 * @returns Formatted date string in Spanish format
 */
export const formatDateTime = (dateString: string | undefined): string => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

/**
 * Format a date string to time only
 * @param dateString - ISO date string
 * @returns Formatted time string in Spanish format
 */
export const formatTime = (dateString: string | undefined): string => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit'
    });
};

/**
 * Format a date string to date only
 * @param dateString - ISO date string
 * @returns Formatted date string in Spanish format
 */
export const formatDate = (dateString: string | undefined): string => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
};
