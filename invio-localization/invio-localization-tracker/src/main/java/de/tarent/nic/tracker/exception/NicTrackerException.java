package de.tarent.nic.tracker.exception;

/**
 * This exception should be used when any exception is thrown while tracking.
 *
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class NicTrackerException extends Exception {

    /**
     * Default constructor.
     */
    public NicTrackerException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param message the message to be shown
     */
    public NicTrackerException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause the cause of the exception
     */
    public NicTrackerException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message the message to be shown
     * @param cause the cause of the exception
     */
    public NicTrackerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
