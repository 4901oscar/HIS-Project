# Audio Notification Files

## Required File

This directory should contain the audio notification file used by the laboratory module:

- **File name**: `notification.mp3` (or `notification.wav`)
- **Duration**: Between 0.5 and 2 seconds
- **Purpose**: Plays when a lab technician clicks the "Atender" button in the appointment list
- **Consistency**: Should use the same audio file as the triage and doctor modules for consistency

## Installation Instructions

1. Obtain or create a short notification sound (0.5-2 seconds)
2. Save it as `notification.mp3` in this directory
3. Ensure the file is in a web-compatible format (MP3 or WAV)
4. Test the audio playback in the browser

## Alternative Formats

If MP3 is not available, you can use:
- `notification.wav` - Uncompressed audio format
- `notification.ogg` - Open-source audio format

The `AudioNotification.ts` utility will attempt to load `notification.mp3` by default.

## Notes

- The audio file is loaded from `/sounds/notification.mp3` in the public directory
- The system handles audio playback errors gracefully
- If the audio file is missing or fails to play, navigation will continue normally
