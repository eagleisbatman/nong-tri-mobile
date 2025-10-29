# Maestro MCP Server Setup & Video Recording Guide

## 🎥 Maestro Features for Testing

Yes! Maestro supports:
- ✅ **Video Recording** - Records screen during test execution
- ✅ **HTML Reports** - Beautiful, user-friendly test reports
- ✅ **Screenshots** - Capture at any point in the flow
- ✅ **MCP Server** - Model Context Protocol integration for Claude Code
- ✅ **CI/CD Integration** - GitHub Actions, Jenkins, etc.

---

## 📦 Installation

### 1. Install Maestro CLI

```bash
# Install via curl
curl -Ls "https://get.maestro.mobile.dev" | bash

# Verify installation
maestro --version
```

### 2. Install Maestro Studio (Optional but Recommended)

Download from: https://maestro.mobile.dev/downloads

---

## 🎬 Video Recording Setup

### Enable Video Recording in Tests

Maestro automatically records video when you run tests with the `--format` flag:

```bash
# Run with HTML report (includes video)
maestro test --format html --output report.html .maestro/

# Run specific test with video
maestro test --format html --output basic_chat.html .maestro/01_basic_chat_flow.yaml
```

### Video Output Locations

By default, videos are saved to:
- **Maestro Cloud**: If using cloud (automatic)
- **Local**: `~/.maestro/tests/<test-run-id>/recording.mp4`

---

## 📊 Test Reporting

### 1. HTML Reports (Recommended)

HTML reports include:
- Video recording of the entire test
- Screenshots at each step
- Pass/fail status with timing
- Detailed error messages
- Interactive timeline

```bash
# Generate HTML report
maestro test --format html --output report.html .maestro/

# Open report in browser
open report.html
```

### 2. JUnit XML Reports (for CI/CD)

```bash
# Generate JUnit XML
maestro test --format junit --output results.xml .maestro/
```

### 3. JSON Reports (for custom processing)

```bash
# Generate JSON report
maestro test --format json --output results.json .maestro/
```

---

## 🔌 Maestro MCP Server Setup

The Maestro MCP server allows Claude Code to run tests programmatically!

### Prerequisites

Maestro MCP requires **Java 17 or higher** to be installed on your system.

```bash
# Check Java version
java -version

# If not installed, install Java 17+
# On macOS with Homebrew:
brew install openjdk@17
```

### 1. Maestro MCP is Built-In

As of Maestro 2.0+, the MCP server is **built directly into the Maestro CLI**. No separate installation needed!

You already installed Maestro CLI in the first section, so you're ready to go.

### 2. Verify MCP Command

```bash
# Test the MCP command
maestro mcp

# Should show: "Starts the Maestro MCP server..."
```

### 3. Configure MCP in Claude Code

#### For Claude Code CLI (Recommended)

Use the built-in MCP configuration command:

```bash
# Add Maestro MCP server with working directory
claude mcp add --transport stdio maestro -- \
  /Users/eagleisbatman/.maestro/bin/maestro mcp \
  --working-dir=/Users/eagleisbatman/nong_tri_workspace/mobile

# Or if maestro is in your PATH:
claude mcp add --transport stdio maestro -- \
  maestro mcp --working-dir=/Users/eagleisbatman/nong_tri_workspace/mobile
```

#### For Claude Desktop

Add to your Claude Desktop config (`~/Library/Application Support/Claude/claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "maestro": {
      "command": "/Users/eagleisbatman/.maestro/bin/maestro",
      "args": [
        "mcp",
        "--working-dir=/Users/eagleisbatman/nong_tri_workspace/mobile"
      ]
    }
  }
}
```

Then restart Claude Desktop.

### 4. Verify MCP Connection

#### Claude Code CLI

```bash
# List configured MCP servers
claude mcp list

# You should see:
# maestro: ... - ✓ Connected
```

Or in the CLI session, type:
```
/mcp
```

#### Claude Desktop

Type `/mcp` in Claude Desktop chat to verify the connection.

---

## 🎯 Enhanced Test Flows with Video & Screenshots

### Example: Enhanced Test with Video Markers

```yaml
appId: com.nongtri.app
name: Chat Test with Video Recording
---
- launchApp

# Add video markers with comments
- runScript: echo "Starting chat interaction"

- tapOn: "Type your message..."
- inputText: "Hello"

# Take screenshot at critical points
- takeScreenshot: "01_message_typed"

- tapOn:
    id: ".*send.*"

# Add delay to capture streaming
- wait: 3000
- takeScreenshot: "02_response_received"

- assertVisible:
    text: ".*Nông Trí.*"

# Final screenshot
- takeScreenshot: "03_conversation_complete"
```

---

## 🚀 Running Tests with Full Reporting

### Single Test with Video

```bash
# Run with HTML report
maestro test \
  --format html \
  --output reports/basic_chat.html \
  .maestro/01_basic_chat_flow.yaml

# Open report
open reports/basic_chat.html
```

### Full Test Suite with Video

```bash
# Create reports directory
mkdir -p reports

# Run all tests with HTML report
maestro test \
  --format html \
  --output reports/full_suite.html \
  .maestro/

# Open report
open reports/full_suite.html
```

### Continuous Mode (for debugging)

```bash
# Run tests continuously on file changes
maestro test --continuous .maestro/
```

---

## 📹 Advanced Video Recording Options

### 1. Custom Video Quality

Maestro doesn't have built-in quality settings, but you can:
- Use screen recording tools alongside Maestro
- Post-process videos with `ffmpeg`

```bash
# Example: Compress video after test
ffmpeg -i ~/.maestro/tests/*/recording.mp4 -vcodec h264 -acodec mp2 compressed.mp4
```

### 2. Separate Screenshots

```yaml
# In your test flow
- takeScreenshot: "step_name"
```

Screenshots are saved to: `~/.maestro/tests/<test-run-id>/screenshots/`

---

## 🔄 CI/CD Integration with Reports

### GitHub Actions Example

```yaml
name: Maestro Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install Maestro
        run: curl -Ls "https://get.maestro.mobile.dev" | bash

      - name: Run Tests with Video
        run: |
          maestro test \
            --format html \
            --output test-report.html \
            .maestro/

      - name: Upload Test Report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-report
          path: test-report.html

      - name: Upload Test Videos
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-videos
          path: ~/.maestro/tests/**/recording.mp4
```

---

## 📱 Using Maestro Cloud (Enhanced Reporting)

Maestro Cloud provides the best reporting experience:

### 1. Sign Up

```bash
maestro cloud auth
```

### 2. Upload and Run Tests

```bash
# Upload APK and run tests
maestro cloud \
  --app-file mobile/composeApp/build/outputs/apk/debug/composeApp-debug.apk \
  --flows .maestro/ \
  --device "Google Pixel 6"
```

### 3. View Results

Maestro Cloud provides:
- 🎥 High-quality video recordings
- 📊 Interactive test reports
- 📈 Historical test trends
- 🔔 Slack/Email notifications
- 🌐 Shareable test results URLs

---

## 🎨 Custom Report Templates

### Create Custom HTML Report

You can process Maestro's JSON output to create custom reports:

```javascript
// generate-report.js
const fs = require('fs');
const results = JSON.parse(fs.readFileSync('results.json', 'utf8'));

const html = `
<!DOCTYPE html>
<html>
<head>
    <title>Nông Trí Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .pass { color: green; }
        .fail { color: red; }
        video { max-width: 800px; }
    </style>
</head>
<body>
    <h1>Test Results</h1>
    <p class="${results.passed ? 'pass' : 'fail'}">
        Status: ${results.passed ? 'PASSED' : 'FAILED'}
    </p>
    <video controls src="${results.videoPath}"></video>
</body>
</html>
`;

fs.writeFileSync('custom-report.html', html);
```

---

## 🛠️ Troubleshooting

### Video Not Recording

1. Check device settings:
```bash
# Enable screen recording permissions
adb shell appops set com.android.shell PROJECT_MEDIA allow
```

2. Ensure storage space:
```bash
# Check available space
df -h ~/.maestro/tests/
```

### Report Not Generating

1. Ensure output directory exists:
```bash
mkdir -p reports
```

2. Check Maestro version:
```bash
maestro --version
# Should be >= 1.36.0 for HTML reports
```

---

## 📚 Best Practices

1. **Name Screenshots Descriptively**: `takeScreenshot: "01_user_login"`
2. **Add Waits Before Screenshots**: Ensure UI is stable
3. **Use HTML Reports for Debugging**: Videos show exactly what happened
4. **Archive Reports**: Keep historical test results
5. **Share Reports**: HTML reports are self-contained, easy to share

---

## 🎯 Quick Commands Cheat Sheet

```bash
# Run tests with video + HTML report
maestro test --format html --output report.html .maestro/

# Run specific test
maestro test .maestro/01_basic_chat_flow.yaml

# Run with tags
maestro test --tags=smoke .maestro/

# Continuous mode (watch for changes)
maestro test --continuous .maestro/

# Upload to Maestro Cloud
maestro cloud --app-file app.apk --flows .maestro/
```

---

## 📖 Additional Resources

- **Maestro Docs**: https://maestro.mobile.dev/docs
- **MCP Server**: https://github.com/mobile-dev-inc/maestro-mcp-server
- **Examples**: https://github.com/mobile-dev-inc/maestro/tree/main/maestro-test/src/test/resources/samples
- **Community**: https://discord.gg/maestro

---

## ✅ Next Steps

1. Install Maestro CLI: `curl -Ls "https://get.maestro.mobile.dev" | bash`
2. Run your first test: `maestro test .maestro/01_basic_chat_flow.yaml`
3. Generate HTML report: `maestro test --format html --output report.html .maestro/`
4. Open and review: `open report.html`

The HTML report will include a full video recording of your app being tested! 🎉