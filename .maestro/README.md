# Maestro Testing Guide for Nông Trí App

## Installation

### 1. Install Maestro (if not already installed)
```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
```

### 2. Install Maestro Studio Community Edition
Download from: https://maestro.mobile.dev/downloads

## Running Tests

### Using Maestro CLI

```bash
# Run all tests
maestro test .maestro/

# Run a specific test
maestro test .maestro/01_basic_chat_flow.yaml

# Run smoke tests only
maestro test --tags=smoke .maestro/

# Run with specific device
maestro test --device="Pixel_6" .maestro/
```

### Using Maestro Studio (Recommended)

1. **Launch Maestro Studio**
2. **Connect your device/emulator** (should auto-detect)
3. **Open the test folder**: File → Open → Select `.maestro` folder
4. **Run tests visually**: Click on any test file to run it
5. **Interactive mode**: Use the record feature to create new tests

## Test Scenarios

| Test File | Description | What it Tests |
|-----------|-------------|---------------|
| `01_basic_chat_flow.yaml` | Basic messaging | Sending messages, receiving responses, streaming |
| `02_agricultural_query.yaml` | Agricultural questions | Tool usage, follow-up questions, classification |
| `03_streaming_test.yaml` | Content streaming | Smooth streaming, typing indicator, markdown formatting |
| `04_auto_scroll_test.yaml` | Auto-scroll behavior | Scroll to bottom, FAB button, message visibility |
| `05_language_test.yaml` | Language switching | Vietnamese language, menu navigation |

## Tips for Maestro Studio

### Visual Testing Mode
1. Click "Play" button to run the selected test
2. Watch the test execute step-by-step
3. See real-time device screen and logs
4. Pause/resume tests for debugging

### Recording New Tests
1. Click "Record" button
2. Interact with your app
3. Maestro captures your actions
4. Save as a new test flow

### Debugging Failed Tests
1. Run test in Studio (not CLI) for better visibility
2. Use "Step" mode to go through one command at a time
3. Check the screenshot folder for captured images
4. Add `- takeScreenshot: "debug_point"` to capture specific states

## Common Issues & Solutions

### Issue: Test can't find elements
**Solution**: Use more flexible selectors
```yaml
# Instead of exact text
- tapOn: "Send"

# Use regex or partial match
- tapOn:
    text: ".*[Ss]end.*"
```

### Issue: Timing problems
**Solution**: Add appropriate waits
```yaml
# Wait for animations
- waitForAnimationToEnd

# Extended wait with condition
- extendedWaitUntil:
    visible:
      text: "Expected text"
    timeout: 10000
```

### Issue: Flaky tests
**Solution**: Make tests more resilient
```yaml
# Use conditional actions
- runFlow:
    when:
      visible: "Optional element"
    file: handle_optional.yaml
```

## Integration with CI/CD

### GitHub Actions Example
```yaml
name: Maestro Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v2

      - name: Install Maestro
        run: curl -Ls "https://get.maestro.mobile.dev" | bash

      - name: Run Maestro tests
        run: maestro test .maestro/
```

## Best Practices

1. **Keep tests focused**: One feature per test file
2. **Use descriptive names**: Clear file and test names
3. **Add comments**: Explain complex interactions
4. **Take screenshots**: Capture important states for debugging
5. **Use tags**: Organize tests into suites (smoke, regression, etc.)
6. **Handle variations**: Account for different device sizes/languages

## Troubleshooting in Maestro Studio

1. **Device not detected**:
   - Ensure USB debugging is enabled
   - Restart ADB: `adb kill-server && adb start-server`

2. **App not launching**:
   - Verify appId in test files matches your app
   - Check app is installed: `adb shell pm list packages | grep nongtri`

3. **Slow performance**:
   - Close other apps on device
   - Use physical device instead of emulator
   - Reduce animation scale in device settings

## Contact & Support

- Maestro Documentation: https://maestro.mobile.dev/docs
- Community: https://github.com/mobile-dev-inc/maestro
- Issues: Report in the `.maestro/` folder or Maestro GitHub