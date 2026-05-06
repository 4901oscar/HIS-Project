/**
 * Audio Notification Utility
 * Plays a notification sound for the laboratory module when the "Atender" button is clicked.
 * Errors are handled gracefully — audio failure never blocks navigation.
 */

export const playNotificationSound = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    try {
      const audio = new Audio('/sounds/notification.mp3');
      audio.volume = 0.7;
      audio.preload = 'auto';

      audio.addEventListener('ended', () => resolve());

      audio.addEventListener('error', () => {
        const code = audio.error?.code ?? 0;
        reject(new Error(`Audio error (code: ${code})`));
      });

      const playPromise = audio.play();
      if (playPromise !== undefined) {
        playPromise.catch((err: Error) => reject(err));
      }
    } catch (err) {
      reject(err);
    }
  });
};

/**
 * Plays the notification sound then executes a callback regardless of success/failure.
 * @param callback - Function to execute after the audio attempt
 * @param delay - Delay in ms before executing callback (default: 500ms)
 */
export const playNotificationWithCallback = (
  callback: () => void,
  delay: number = 500
): void => {
  playNotificationSound()
    .catch(() => {
      // Audio failure is non-blocking — continue to callback
    })
    .finally(() => {
      setTimeout(callback, delay);
    });
};

/**
 * Checks if the notification audio file is available.
 */
export const checkAudioAvailability = async (): Promise<boolean> => {
  try {
    const response = await fetch('/sounds/notification.mp3', { method: 'HEAD' });
    return response.ok;
  } catch {
    return false;
  }
};
