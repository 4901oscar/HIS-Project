# Audio Notification Utility

## Overview

The `AudioNotification.ts` utility provides audio notification functionality for the laboratory module. It plays a notification sound when lab technicians click the "Atender" button in the appointment list.

## Features

- ✅ HTML5 Audio API integration
- ✅ Graceful error handling (audio failures don't block navigation)
- ✅ Console logging for debugging
- ✅ Promise-based API
- ✅ Callback wrapper for convenience
- ✅ Audio availability checking

## Requirements Validated

- **8.2**: Play audio notification when "Atender" button is clicked
- **8.3**: Handle audio playback errors gracefully without blocking navigation
- **13.6**: Log errors to console for debugging

## Usage

### Basic Usage

```typescript
import { playNotificationSound } from '../utils/AudioNotification';

// Play notification sound
playNotificationSound()
  .then(() => console.log('Audio played successfully'))
  .catch(() => console.log('Audio failed, but navigation continues'));
```

### With Navigation Callback

```typescript
import { playNotificationWithCallback } from '../utils/AudioNotification';
import { useNavigate } from 'react-router-dom';

const navigate = useNavigate();

// Play notification and navigate after delay
playNotificationWithCallback(() => {
  navigate('/lab/workflow/123');
}, 500); // 500ms delay
```

### Check Audio Availability

```typescript
import { checkAudioAvailability } from '../utils/AudioNotification';

// Check if audio file exists
const isAvailable = await checkAudioAvailability();
if (!isAvailable) {
  console.warn('Audio notification file not found');
}
```

## Integration Example

### Lab Appointment List

```typescript
import { playNotificationWithCallback } from '../../utils/AudioNotification';

const handleAtender = (appointmentId: string) => {
  playNotificationWithCallback(() => {
    navigate(`/lab/workflow/${appointmentId}`);
  }, 500);
};

return (
  <button onClick={() => handleAtender(appointment.id)}>
    Atender
  </button>
);
```

## Audio File Setup

1. Place `notification.mp3` in `/public/sounds/` directory
2. Ensure file duration is between 0.5 and 2 seconds
3. Use the same audio file as triage and doctor modules for consistency

## Error Handling

The utility handles errors gracefully:

- **Missing audio file**: Logs warning, navigation continues
- **Browser autoplay policy**: Logs warning, navigation continues
- **Unsupported format**: Logs warning, navigation continues
- **Audio constructor failure**: Logs error, navigation continues

All errors are logged to the console for debugging purposes.

## Testing

Unit tests are located in `__tests__/AudioNotification.test.ts` and cover:

- Audio element creation
- Successful playback
- Error handling
- Callback execution
- Audio availability checking

Run tests with:

```bash
npm test -- AudioNotification.test.ts
```

## Browser Compatibility

The utility uses the HTML5 Audio API, which is supported in all modern browsers:

- Chrome/Edge: ✅
- Firefox: ✅
- Safari: ✅
- Opera: ✅

**Note**: Some browsers may block autoplay until the user has interacted with the page. The utility handles this gracefully.

## Troubleshooting

### Audio doesn't play

1. Check if `notification.mp3` exists in `/public/sounds/`
2. Check browser console for error messages
3. Verify browser autoplay policy allows audio
4. Try clicking the button after interacting with the page

### Audio file not found

1. Ensure file is in `/public/sounds/notification.mp3`
2. Check file permissions
3. Verify file format is web-compatible (MP3, WAV, OGG)

### Tests failing

1. Ensure Vitest is properly configured
2. Check that mocks are using class syntax (not `mockReturnValue`)
3. Verify console spies are properly restored in `afterEach`

## Future Enhancements

- Support for multiple notification sounds
- Volume control settings
- User preference for enabling/disabling audio
- Fallback to alternative audio formats (WAV, OGG)
