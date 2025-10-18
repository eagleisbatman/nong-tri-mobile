# Implementation Plan - Prioritized by Deliverability

## Current State Audit

### ✅ **What's Working:**
- OpenAI GPT-4o integration with streaming
- Mobile app with Material 3 UI
- Language selection (Vietnamese/English)
- Basic chat interface
- Markdown rendering

### ❓ **Unknown/Needs Verification:**
- Database connection (PostgreSQL on Railway) - Code exists but unclear if connected
- Redis caching - Code exists but may not be active
- Conversation history persistence

### ❌ **Not Implemented:**
- User profiles
- Location tracking
- Crop/livestock management
- Memory/RAG system
- Dark/light mode toggle
- Response tone/length configuration

---

## Priority Matrix (Quick Wins → Complex Features)

### 🟢 **TIER 1: Quick Wins (1-2 hours each) - Ship This Week**

#### 1. Dark/Light Mode Toggle ⚡
**Complexity:** LOW | **Value:** HIGH | **Dependencies:** None
- Android has built-in system dark mode detection
- Just need toggle in settings
- Material 3 already handles color schemes
- **Steps:**
  - Add theme preference to UserPreferences
  - Add toggle in settings menu
  - Apply theme based on preference

#### 2. "You" and "AI" Labels on Bubbles ⚡
**Complexity:** VERY LOW | **Value:** MEDIUM | **Dependencies:** None
- Simple UI change to MessageBubble.kt
- Add small text label above bubble
- **Steps:**
  - Add Text composable for "You" / "Nông Trí AI"
  - Style with caption typography

#### 3. Timestamp to Top of Bubble ⚡
**Complexity:** VERY LOW | **Value:** LOW | **Dependencies:** None
- Move existing timestamp from bottom to top
- **Steps:**
  - Reorder composables in MessageBubble

#### 4. New Chat Button (FAB) ⚡
**Complexity:** LOW | **Value:** HIGH | **Dependencies:** None
- Add FloatingActionButton to clear chat
- Icon: add/new icon
- **Steps:**
  - Add FAB to ChatScreen
  - Call viewModel.clearHistory() on click

---

### 🟡 **TIER 2: Medium Effort (3-6 hours each) - Ship Next Week**

#### 5. Copy Button Below AI Messages 📋
**Complexity:** LOW | **Value:** HIGH | **Dependencies:** Clipboard API
- Add IconButton below AI bubble
- Use Android clipboard manager
- **Steps:**
  - Add Row of action buttons below AI messages
  - Implement clipboard copy
  - Show toast/snackbar on copy

#### 6. Share Button with Bottom Sheet 📤
**Complexity:** MEDIUM | **Value:** MEDIUM | **Dependencies:** Share API
- Add share icon button
- Show ModalBottomSheet with share options
- **Steps:**
  - Create ShareBottomSheet composable
  - Integrate Android share intent
  - Share as text or image

#### 7. Thumbs Up/Down Feedback 👍👎
**Complexity:** LOW | **Value:** HIGH | **Dependencies:** Database
- Add feedback buttons below AI messages
- Store feedback in database for future training
- **Steps:**
  - Add IconButtons for up/down
  - Create feedback API endpoint
  - Save to database

#### 8. Response Tone & Length Configuration ⚙️
**Complexity:** MEDIUM | **Value:** HIGH | **Dependencies:** System prompt modification
- Add settings for: Tone (Friendly/Professional/Technical), Length (Brief/Normal/Detailed)
- Modify system prompt based on preferences
- **Steps:**
  - Add preferences UI
  - Update system prompt dynamically
  - Test variations

#### 9. Conversation History Bottom Sheet 📜
**Complexity:** MEDIUM | **Value:** HIGH | **Dependencies:** Database
- Move from menu to bottom sheet
- Show list of past conversations
- Include "Clear History" inside
- **Steps:**
  - Create HistoryBottomSheet composable
  - Fetch history from API
  - Add delete/clear actions

---

### 🟠 **TIER 3: Significant Effort (1-2 days each) - Ship in 2 Weeks**

#### 10. Location Sharing (Share → Update Location) 📍
**Complexity:** MEDIUM-HIGH | **Value:** HIGH | **Dependencies:** Location permission, GPS
- Request location permission
- Get GPS coordinates
- Display in UI (title bar or below name)
- Send location context to AI
- **Steps:**
  - Add location permission to manifest
  - Implement location fetching
  - Store in UserPreferences
  - Display in UI
  - Inject into AI context

#### 11. User Profile Screen 👤
**Complexity:** MEDIUM | **Value:** HIGH | **Dependencies:** Database, Navigation
- Screen for: Name, Age, Gender, Farming Type, Location
- Store in database
- **Steps:**
  - Create ProfileScreen composable
  - Add navigation
  - Create profile API endpoints
  - Form validation and save

#### 12. Text-to-Speech (Listen Button) 🔊
**Complexity:** MEDIUM-HIGH | **Value:** MEDIUM | **Dependencies:** TTS API
- Add speaker icon below AI messages
- Use Android TTS to read response
- Play/pause controls
- **Steps:**
  - Integrate Android TextToSpeech
  - Add playback controls
  - Handle different languages (EN/VI)

---

### 🔴 **TIER 4: Complex Features (3-7 days each) - Ship in 1 Month**

#### 13. Crop Management System 🌾
**Complexity:** HIGH | **Value:** HIGH | **Dependencies:** Database schema, UI screens
- Add/edit/delete crops
- Track planting dates, harvest dates
- Link to AI context
- **Steps:**
  - Design crop database schema
  - Create CRUD API endpoints
  - Build crop management UI
  - Inject crop data into AI context

#### 14. Livestock Management System 🐄
**Complexity:** HIGH | **Value:** HIGH | **Dependencies:** Database schema, UI screens
- Similar to crop management
- Track animals, health records
- **Steps:**
  - Design livestock database schema
  - Create CRUD API endpoints
  - Build livestock management UI
  - Inject livestock data into AI context

#### 15. Context Decision Tree & Injection System 🌳
**Complexity:** HIGH | **Value:** VERY HIGH | **Dependencies:** All user data
- Build logic to determine what context to send
- Priority: Location > Crops/Livestock > User Profile > History
- Token budget management
- **Steps:**
  - Design context builder algorithm
  - Implement priority system
  - Test token usage
  - Optimize context relevance

---

### 🟣 **TIER 5: Advanced Features (1-2 weeks each) - Ship in 2+ Months**

#### 16. RAG-Based Memory System 🧠
**Complexity:** VERY HIGH | **Value:** VERY HIGH | **Dependencies:** Vector DB, Embeddings
- Store conversation embeddings
- Semantic search for relevant past context
- Long-term memory across sessions
- **Steps:**
  - Choose vector DB (Pinecone/Qdrant/PostgreSQL pgvector)
  - Generate embeddings for conversations
  - Implement semantic search
  - Inject relevant memories into context

#### 17. Database Schema for User Memory 🗄️
**Complexity:** HIGH | **Value:** HIGH | **Dependencies:** RAG system
- Design schema for:
  - User profiles
  - Crops/livestock
  - Conversations with embeddings
  - Feedback data
  - Context metadata
- **Steps:**
  - Design normalized schema
  - Write migrations
  - Implement repositories
  - Add indexes for performance

---

## Recommended Implementation Order (Next 4 Weeks)

### Week 1: Quick Wins + Database Audit
1. ✅ Audit database/Redis connectivity
2. 🎨 Dark/Light mode toggle
3. 🏷️ "You"/"AI" labels
4. ⏱️ Timestamp repositioning
5. ➕ New Chat button
6. 📋 Copy button

### Week 2: User Actions + Configuration
7. 📤 Share button with bottom sheet
8. 👍👎 Feedback buttons
9. ⚙️ Tone & length settings
10. 📜 Conversation history bottom sheet

### Week 3: Location + Profile
11. 📍 Location sharing
12. 👤 User profile screen

### Week 4: Context System + TTS
13. 🌳 Basic context injection (location + profile)
14. 🔊 Text-to-speech

### Month 2+: Management Systems
15. 🌾 Crop management
16. 🐄 Livestock management
17. 🧠 RAG memory system (if needed)

---

## Key Questions to Answer First

1. **Is the Railway database actually connected?**
   - Need to check Railway dashboard
   - Test with actual database query

2. **Is Redis working?**
   - Check if caching is active
   - May not be needed initially

3. **Current data persistence:**
   - Are conversations being saved?
   - Where is user data stored?

4. **Authentication:**
   - How are users identified?
   - Do we need proper auth?

---

## Next Immediate Actions

1. **Verify database connectivity** (30 min)
2. **Implement dark mode** (1 hour)
3. **Add message labels and reposition timestamp** (30 min)
4. **Add New Chat button** (30 min)
5. **Add copy button** (1 hour)

**Total for immediate release: ~4 hours of work**

After these are done, we have a much more polished app ready for real users!
