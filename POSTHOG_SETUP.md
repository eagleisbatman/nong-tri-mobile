# PostHog Configuration Guide

## Current Status

- ✅ **Backend PostHog**: Working (server-side events)
- ✅ **PostHog MCP Server**: Working (for AI assistants)
- ⚠️ **Mobile PostHog SDK**: Not configured (needs API key + SDK fix)

## Mobile App Configuration

### Step 1: Get Your PostHog Project API Key

1. Go to [PostHog Dashboard](https://app.posthog.com)
2. Navigate to **Project Settings** → **Project API Key**
3. Copy your API key (starts with `phc_`)

### Step 2: Configure in local.properties

Add to `mobile/local.properties`:

```properties
posthog.api.key=phc_YOUR_API_KEY_HERE
posthog.api.host=https://us.i.posthog.com
```

**Note**: `local.properties` is gitignored and won't be committed.

### Step 3: Rebuild and Test

```bash
cd mobile
./gradlew clean
./gradlew :composeApp:assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Step 4: Verify

Check logs for:
```
[AnalyticsService] ✅ PostHog initialized with API key: phc_xxxxx...
```

Then check PostHog dashboard → **Live Events** for mobile app events.

## PostHog MCP Server (For AI Assistants)

✅ **Already configured** in `~/.cursor/mcp.json`

The MCP server allows AI assistants to query PostHog data, manage feature flags, and create annotations.

## Backend PostHog

✅ **Already working** - Configured via environment variables:
- `POSTHOG_API_KEY`
- `POSTHOG_HOST`

## Troubleshooting

**Mobile events not appearing?**
- Verify API key in `local.properties`
- Check network connection
- Rebuild app after adding credentials
- Check PostHog dashboard → Live Events

**MCP server not working?**
- Restart Cursor after configuration changes
- Check `~/.cursor/mcp.json` has correct API key
- Verify Personal API Key (not Project API Key) is used

## Resources

- [PostHog Android SDK](https://posthog.com/docs/integrate/client/android)
- [PostHog MCP Server](https://posthog.com/docs/model-context-protocol)
- [PostHog Dashboard](https://app.posthog.com)
