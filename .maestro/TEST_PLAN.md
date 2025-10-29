# Maestro Test Plan - Nông Trí App

## Test Coverage Using Test Tags

This document outlines comprehensive test scenarios using the test tags defined in `test-tags.csv`.

---

## 📋 Test Suite Overview

### 1. Basic Chat Flow (01_basic_chat_flow.yaml)
**Purpose**: Test core messaging functionality with text input and AI responses

**Test Tags Used**:
- `chat_screen` - Verify main chat screen loads
- `text_field` - Input text messages
- `send_button` - Send messages
- `message_list` - Verify message display
- `welcome_card` - Check welcome message
- `starter_question_0` - Test starter question clicks
- `scroll_to_bottom_fab` - Verify FAB appears/disappears

**Test Steps**:
1. Launch app → Assert `chat_screen` visible
2. Verify `welcome_card` appears for new users
3. Tap `text_field` → Input "Hello"
4. Tap `send_button` → Verify message sent
5. Wait for AI response → Assert message appears in `message_list`
6. Scroll up → Verify `scroll_to_bottom_fab` appears
7. Tap `scroll_to_bottom_fab` → Verify scrolls to bottom

---

### 2. Agricultural Query Flow (02_agricultural_query.yaml)
**Purpose**: Test agricultural-specific queries and follow-up questions

**Test Tags Used**:
- `text_field`
- `send_button`
- `message_text_0`, `message_text_1`, etc.
- `follow_up_question_0`, `follow_up_question_1`
- `starter_question_0`

**Test Steps**:
1. Launch app
2. Tap `starter_question_0` (agricultural question)
3. Verify AI response with agricultural content
4. Assert `follow_up_question_0` appears
5. Tap `follow_up_question_0`
6. Verify second AI response
7. Take screenshots at each step

---

### 3. Auto-Scroll Test (04_auto_scroll_test.yaml)
**Purpose**: Verify auto-scroll behavior with multiple messages

**Test Tags Used**:
- `message_list`
- `scroll_to_bottom_fab`
- `text_field`
- `send_button`
- `message_bubble_0`, `message_bubble_1`, etc.

**Test Steps**:
1. Send 3-4 messages to create scrollable content
2. Scroll up manually
3. Verify `scroll_to_bottom_fab` appears
4. Send new message while scrolled up
5. Verify auto-scroll to bottom (FAB disappears)
6. Scroll up again
7. Tap `scroll_to_bottom_fab`
8. Verify scrolls to bottom smoothly

---

### 4. Language Selection Test (05_language_test.yaml)
**Purpose**: Test language switching functionality

**Test Tags Used**:
- `menu_button`
- `menu_language_vi`
- `menu_language_en`
- `text_field`
- `send_button`

**Test Steps**:
1. Launch app
2. Tap `menu_button`
3. Tap `menu_language_vi` (Vietnamese)
4. Send message in Vietnamese
5. Verify Vietnamese response
6. Switch back to `menu_language_en` (English)
7. Verify English response

---

### 5. Menu Navigation Test (NEW)
**Purpose**: Test all menu options

**Test Tags Used**:
- `menu_button`
- `menu_conversations`
- `menu_new_chat`
- `menu_share_location`
- `profile_button`

**Test Steps**:
1. Tap `menu_button`
2. Verify all menu items visible:
   - `menu_conversations`
   - `menu_new_chat`
   - `menu_share_location`
   - `menu_language_vi`, `menu_language_en`
3. Tap `menu_conversations` → Verify navigation
4. Tap `back_button` → Return to chat
5. Tap `menu_new_chat` → Verify new chat started

---

### 6. Voice Recording Test (NEW)
**Purpose**: Test voice recording functionality

**Test Tags Used**:
- `voice_button`
- `voice_recording_overlay`
- `voice_recording_bar`
- `voice_animation`
- `voice_cancel_button`
- `voice_send_button`

**Test Steps**:
1. Tap `voice_button`
2. Verify `voice_recording_overlay` appears
3. Assert `voice_animation` is visible
4. Verify `voice_recording_bar` shows
5. Tap `voice_cancel_button` → Recording cancelled
6. Tap `voice_button` again
7. Tap `voice_send_button` → Voice message sent

---

### 7. Image Attachment Test (NEW)
**Purpose**: Test image attachment and diagnosis

**Test Tags Used**:
- `image_button`
- `image_source_sheet`
- `camera_option`
- `gallery_option`
- `image_preview`
- `image_card`
- `question_input`
- `send_diagnosis_button`

**Test Steps**:
1. Tap `image_button`
2. Verify `image_source_sheet` appears
3. Select `gallery_option`
4. Select an image
5. Verify `image_preview` shows
6. Verify `image_card` appears
7. Optionally enter text in `question_input`
8. Tap `send_diagnosis_button`
9. Verify diagnosis response

---

### 8. Message Actions Test (NEW)
**Purpose**: Test message interaction buttons

**Test Tags Used**:
- `copy_button`
- `share_button`
- `tts_button`
- `thumbs_up_button`
- `thumbs_down_button`

**Test Steps**:
1. Send a message and get AI response
2. Long press on AI message bubble
3. Verify action buttons appear:
   - `copy_button`
   - `share_button`
   - `tts_button`
4. Tap `tts_button` → Verify audio plays
5. Tap `thumbs_up_button` → Verify feedback sent
6. Tap `copy_button` → Verify text copied

---

### 9. Location Sharing Test (NEW)
**Purpose**: Test location sharing functionality

**Test Tags Used**:
- `menu_button`
- `menu_share_location`
- `location_bottom_sheet`
- `update_location_button`
- `share_location_button`
- `close_button`

**Test Steps**:
1. Tap `menu_button`
2. Tap `menu_share_location`
3. Verify `location_bottom_sheet` appears
4. Tap `update_location_button`
5. Wait for location update
6. Tap `share_location_button`
7. Verify location shared in chat
8. Tap `close_button`

---

### 10. Conversations List Test (NEW)
**Purpose**: Test conversation thread management

**Test Tags Used**:
- `menu_button`
- `menu_conversations`
- `conversations_screen`
- `conversation_list`
- `conversation_item_123` (dynamic)
- `delete_button_123` (dynamic)
- `delete_dialog`
- `delete_confirm_button`
- `new_conversation_fab`
- `back_button`

**Test Steps**:
1. Create multiple conversations
2. Tap `menu_button` → `menu_conversations`
3. Verify `conversations_screen` loads
4. Verify `conversation_list` shows threads
5. Swipe on `conversation_item_123`
6. Tap `delete_button_123`
7. Verify `delete_dialog` appears
8. Tap `delete_confirm_button`
9. Verify conversation deleted
10. Tap `new_conversation_fab`
11. Tap `back_button` to return

---

## 🎯 Test Execution Order

### Smoke Tests (Quick Validation)
1. Basic Chat Flow
2. Agricultural Query Flow

### Full Suite (Comprehensive)
1. Basic Chat Flow
2. Agricultural Query Flow
3. Auto-Scroll Test
4. Language Selection Test
5. Menu Navigation Test
6. Voice Recording Test
7. Image Attachment Test
8. Message Actions Test
9. Location Sharing Test
10. Conversations List Test

---

## 📊 Test Tag Categories

### Screen Containers
- `chat_screen`
- `conversations_screen`
- `profile_screen`
- `language_selection_screen`

### Input Elements
- `text_field` - Main message input
- `question_input` - Diagnosis question input

### Action Buttons
- `send_button` - Send text message
- `voice_button` - Start voice recording
- `image_button` - Attach image
- `menu_button` - Open menu
- `profile_button` - Open profile/settings

### Message Elements
- `message_list` - Scrollable message list
- `message_bubble_{index}` - Individual message container
- `message_text_{index}` - Message text content
- `welcome_card` - Welcome message

### Interactive Elements
- `starter_question_{index}` - Starter question chips
- `follow_up_question_{index}` - Follow-up question chips
- `scroll_to_bottom_fab` - Scroll to bottom button

### Menu Items
- `menu_conversations`
- `menu_new_chat`
- `menu_share_location`
- `menu_language_vi`, `menu_language_en`
- `menu_theme_light`, `menu_theme_dark`, `menu_theme_system`

---

## ✅ Test Success Criteria

### Basic Chat Flow
- ✅ App launches successfully
- ✅ Welcome card appears
- ✅ Text input works
- ✅ Send button sends message
- ✅ AI response appears
- ✅ Messages display correctly
- ✅ Auto-scroll works

### Agricultural Query
- ✅ Starter questions appear
- ✅ Agricultural content in responses
- ✅ Follow-up questions generated
- ✅ Follow-up questions clickable
- ✅ Second response relevant

### Auto-Scroll
- ✅ FAB appears when scrolled up
- ✅ FAB disappears when at bottom
- ✅ Auto-scroll on new message
- ✅ Manual scroll to bottom works

### Language Selection
- ✅ Menu opens
- ✅ Language options visible
- ✅ Language switches correctly
- ✅ Responses in selected language

---

## 🎬 Video Recording Points

Each test should capture:
- App launch
- User interactions (taps, scrolls, swipes)
- AI responses appearing
- UI state changes
- Error states (if any)
- Success confirmations

Screenshot markers at:
- Before action
- After action
- AI response received
- Final state
