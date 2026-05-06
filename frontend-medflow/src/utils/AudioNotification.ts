/**
 * Audio Notification Utility
 * 
 * Provides audio notification functionality for the laboratory module.
 * Plays a notification sound when lab technicians click the "Atender" button.
 * 
 * Requirements:
 * - 8.2: Play audio notification when "Atender" button is clicked
 * - 8.3: Handle audio playback errors gracefully without blocking navigation
 * - 13.6: Log errors to console for debugging
 */

/**
 * Plays the notification sound from the public assets directory.
 * 
 * This function uses the HTML5 Audio API to play a notification sound.
 * It handles errors gracefully and does not block execution if audio fails.
 * 
 * @returns Promise that resolves when audio finishes playing or rejects on error
 * 
 * @example
 * ```typescript
 * // Play notification before navigation
 * playNotificationSound()
 *   .then(() => console.log('Audio played successfully'))
 *   .catch(() => console.log('Audio failed, but navigation continues'));
 * 
 * // Or use without waiting
 * playNotificationSound();
 * navigate('/lab/workflow/123');
 * ```
 */
export const playNotificationSound = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    try {
      // Create audio element with notification file
      const audio = new Audio('/sounds/notification.mp3');
      
      // Set audio properties
      audio.volume = 0.7; // 70% volume for comfortable listening
      audio.preload = 'auto';
      
      // Handle successful playback completion
      audio.addEventListener('ended', () => {
        console.log('[AudioNotification] Notification sound played successfully');
        resolve();
      });
      
      // Handle audio loading errors
      audio.addEventListener('error', (event) => {
        const error = audio.error;
        const errorMessage = error 
          ? `Audio error: ${error.message} (code: ${error.code})`
          : 'Unknown audio error';
        
        console.warn('[AudioNotification] Failed to load or play notification sound:', errorMessage);
        console.warn('[AudioNotification] Event details:', event);
        
        // Reject but don't throw - caller should handle gracefully
        reject(new Error(errorMessage));
      });
      
      // Attempt to play the audio
      const playPromise = audio.play();
      
      // Handle play promise (modern browsers return a promise)
      if (playPromise !== undefined) {
        playPromise
          .then(() => {
            console.log('[AudioNotification] Audio playback started');
          })
          .catch((error) => {
            // Common reasons for play failure:
            // - User hasn't interacted with the page yet (autoplay policy)
            // - Audio file not found
            // - Unsupported audio format
            // - Browser permissions
            console.warn('[AudioNotification] Audio playback failed:', error.message);
            console.warn('[AudioNotification] This is usually due to browser autoplay policies or missing audio file');
            
            // Reject but don't throw
            reject(error);
          });
      }
    } catch (error) {
      // Catch any synchronous errors (e.g., Audio constructor failure)
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      console.error('[AudioNotification] Failed to create audio element:', errorMessage);
      reject(error);
    }
  });
};

/**
 * Plays the notification sound and executes a callback regardless of success/failure.
 * This is a convenience wrapper that ensures the callback always executes.
 * 
 * @param callback - Function to execute after audio attempt (success or failure)
 * @param delay - Optional delay in milliseconds before executing callback (default: 500ms)
 * 
 * @example
 * ```typescript
 * playNotificationWithCallback(() => {
 *   navigate('/lab/workflow/123');
 * }, 500);
 * ```
 */
export const playNotificationWithCallback = (
  callback: () => void,
  delay: number = 500
): void => {
  playNotificationSound()
    .catch((error) => {
      // Log error but continue - audio failure should not block navigation
      console.warn('[AudioNotification] Audio notification failed, continuing anyway:', error);
    })
    .finally(() => {
      // Execute callback after delay, regardless of audio success/failure
      setTimeout(callback, delay);
    });
};

/**
 * Checks if the notification audio file is available.
 * This is useful for testing or displaying warnings to users.
 * 
 * @returns Promise that resolves to true if audio file exists, false otherwise
 * 
 * @example
 * ```typescript
 * const isAvailable = await checkAudioAvailability();
 * if (!isAvailable) {
 *   console.warn('Audio notification file not found');
 * }
 * ```
 */
export const checkAudioAvailability = async (): Promise<boolean> => {
  try {
    const response = await fetch('/sounds/notification.mp3', { method: 'HEAD' });
    return response.ok;
  } catch (error) {
    console.warn('[AudioNotification] Could not check audio file availability:', error);
    return false;
  }
};
