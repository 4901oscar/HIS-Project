import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { playNotificationSound, playNotificationWithCallback, checkAudioAvailability } from '../AudioNotification';

/**
 * Unit Tests for AudioNotification Utility
 * 
 * **Validates: Requirements 8.2, 8.3, 13.6**
 * 
 * These tests verify that the audio notification system:
 * - Plays notification sounds using HTML5 Audio API
 * - Handles errors gracefully without blocking execution
 * - Logs errors to console for debugging
 */

describe('AudioNotification Utility', () => {
  let mockAudio: any;
  let audioInstances: any[];
  let originalAudio: any;
  let consoleLogSpy: any;
  let consoleWarnSpy: any;
  let consoleErrorSpy: any;

  beforeEach(() => {
    // Store original Audio constructor
    originalAudio = global.Audio;
    
    // Track all audio instances created
    audioInstances = [];
    
    // Mock Audio constructor using class syntax for Vitest
    global.Audio = class MockAudio {
      src: string;
      volume: number = 1;
      preload: string = 'none';
      play = vi.fn().mockResolvedValue(undefined);
      pause = vi.fn();
      load = vi.fn();
      addEventListener = vi.fn();
      removeEventListener = vi.fn();
      error: any = null;

      constructor(src: string) {
        this.src = src;
        audioInstances.push(this);
      }
    } as any;
    
    mockAudio = global.Audio;
    
    // Spy on console methods
    consoleLogSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    // Restore original Audio constructor
    global.Audio = originalAudio;
    
    // Restore console methods
    consoleLogSpy.mockRestore();
    consoleWarnSpy.mockRestore();
    consoleErrorSpy.mockRestore();
    
    // Clear all mocks
    vi.clearAllMocks();
  });

  describe('playNotificationSound', () => {
    it('should create Audio element with correct source path', async () => {
      // Act
      playNotificationSound().catch(() => {}); // Ignore rejection for this test

      // Wait for audio instance to be created
      await new Promise(resolve => setTimeout(resolve, 10));

      // Assert
      expect(audioInstances.length).toBeGreaterThan(0);
      const audioInstance = audioInstances[0];
      expect(audioInstance.src).toBe('/sounds/notification.mp3');
      expect(audioInstance.volume).toBe(0.7);
      expect(audioInstance.preload).toBe('auto');
    });

    it('should resolve when audio playback completes successfully', async () => {
      // Arrange - Override Audio class for this test
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockResolvedValue(undefined);
        addEventListener = vi.fn((event: string, handler: Function) => {
          if (event === 'ended') {
            setTimeout(() => handler(), 10);
          }
        });
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act
      await playNotificationSound();

      // Assert
      expect(consoleLogSpy).toHaveBeenCalledWith(
        expect.stringContaining('Notification sound played successfully')
      );
    });

    it('should handle audio loading errors gracefully', async () => {
      // Arrange - Override Audio class for this test
      const mockError = { code: 4, message: 'MEDIA_ELEMENT_ERROR: Media load error' };
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockResolvedValue(undefined);
        addEventListener = vi.fn((event: string, handler: Function) => {
          if (event === 'error') {
            setTimeout(() => handler({ type: 'error' }), 10);
          }
        });
        error: any = mockError;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act & Assert
      await expect(playNotificationSound()).rejects.toThrow();
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Failed to load or play notification sound'),
        expect.any(String)
      );
    });

    it('should handle play promise rejection gracefully', async () => {
      // Arrange - Override Audio class for this test
      const playError = new Error('NotAllowedError: play() failed');
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockRejectedValue(playError);
        addEventListener = vi.fn();
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act & Assert
      await expect(playNotificationSound()).rejects.toThrow();
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Audio playback failed'),
        expect.any(String)
      );
    });

    it('should log errors to console for debugging (Requirement 13.6)', async () => {
      // Arrange - Override Audio to throw on construction
      const constructorError = new Error('Audio constructor failed');
      global.Audio = class MockAudio {
        constructor() {
          throw constructorError;
        }
      } as any;

      // Act & Assert
      await expect(playNotificationSound()).rejects.toThrow();
      expect(consoleErrorSpy).toHaveBeenCalledWith(
        expect.stringContaining('Failed to create audio element'),
        expect.any(String)
      );
    });
  });

  describe('playNotificationWithCallback', () => {
    it('should execute callback after successful audio playback', async () => {
      // Arrange
      const callback = vi.fn();
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockResolvedValue(undefined);
        addEventListener = vi.fn((event: string, handler: Function) => {
          if (event === 'ended') {
            setTimeout(() => handler(), 0);
          }
        });
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act
      playNotificationWithCallback(callback, 100);

      // Wait for callback to be executed
      await new Promise(resolve => setTimeout(resolve, 150));

      // Assert
      expect(callback).toHaveBeenCalled();
    });

    it('should execute callback even when audio fails (Requirement 8.3)', async () => {
      // Arrange
      const callback = vi.fn();
      const playError = new Error('Audio playback failed');
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockRejectedValue(playError);
        addEventListener = vi.fn();
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act
      playNotificationWithCallback(callback, 100);

      // Wait for callback to be executed
      await new Promise(resolve => setTimeout(resolve, 150));

      // Assert
      expect(callback).toHaveBeenCalled();
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Audio notification failed, continuing anyway'),
        expect.any(Error)
      );
    });

    it('should respect custom delay parameter', async () => {
      // Arrange
      const callback = vi.fn();
      const customDelay = 200;
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockResolvedValue(undefined);
        addEventListener = vi.fn((event: string, handler: Function) => {
          if (event === 'ended') {
            setTimeout(() => handler(), 0);
          }
        });
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act
      const startTime = Date.now();
      playNotificationWithCallback(callback, customDelay);

      // Wait for callback
      await new Promise(resolve => setTimeout(resolve, customDelay + 50));
      const elapsed = Date.now() - startTime;

      // Assert
      expect(callback).toHaveBeenCalled();
      expect(elapsed).toBeGreaterThanOrEqual(customDelay);
    });
  });

  describe('checkAudioAvailability', () => {
    let originalFetch: any;

    beforeEach(() => {
      originalFetch = global.fetch;
    });

    afterEach(() => {
      global.fetch = originalFetch;
    });

    it('should return true when audio file is available', async () => {
      // Arrange
      global.fetch = vi.fn().mockResolvedValue({ ok: true });

      // Act
      const result = await checkAudioAvailability();

      // Assert
      expect(result).toBe(true);
      expect(global.fetch).toHaveBeenCalledWith('/sounds/notification.mp3', { method: 'HEAD' });
    });

    it('should return false when audio file is not available', async () => {
      // Arrange
      global.fetch = vi.fn().mockResolvedValue({ ok: false });

      // Act
      const result = await checkAudioAvailability();

      // Assert
      expect(result).toBe(false);
    });

    it('should return false and log warning when fetch fails', async () => {
      // Arrange
      const fetchError = new Error('Network error');
      global.fetch = vi.fn().mockRejectedValue(fetchError);

      // Act
      const result = await checkAudioAvailability();

      // Assert
      expect(result).toBe(false);
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('Could not check audio file availability'),
        fetchError
      );
    });
  });

  describe('Error Handling and Resilience (Requirement 8.3)', () => {
    it('should not block execution when audio constructor fails', async () => {
      // Arrange
      global.Audio = class MockAudio {
        constructor() {
          throw new Error('Audio not supported');
        }
      } as any;

      // Act & Assert
      await expect(playNotificationSound()).rejects.toThrow();
      // The important part is that it rejects gracefully, not throws synchronously
    });

    it('should handle missing audio file gracefully', async () => {
      // Arrange
      global.Audio = class MockAudio {
        src: string;
        volume: number = 1;
        preload: string = 'none';
        play = vi.fn().mockRejectedValue(new Error('Failed to load'));
        addEventListener = vi.fn();
        error: any = null;

        constructor(src: string) {
          this.src = src;
        }
      } as any;

      // Act
      const promise = playNotificationSound();

      // Assert
      await expect(promise).rejects.toThrow();
      expect(consoleWarnSpy).toHaveBeenCalled();
    });
  });
});
