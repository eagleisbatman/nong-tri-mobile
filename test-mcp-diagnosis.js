const https = require('https');
const fs = require('fs');

// Test plant leaf image (base64 encoded - small diseased leaf sample)
const testImageBase64 = 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAyADIDAREAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlbaWmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP//Z';

const requestBody = JSON.stringify({
  device_id: 'test-device-123',
  message: 'What disease does this plant have?',
  image: testImageBase64,
  conversation_id: null
});

const options = {
  hostname: 'nong-tri.up.railway.app',
  port: 443,
  path: '/api/chat',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(requestBody)
  }
};

console.log('\n🧪 Testing AgriVision MCP Server via /api/chat');
console.log('📡 Endpoint: https://nong-tri.up.railway.app/api/chat');
console.log('🖼️  Image: Test plant leaf (base64)');
console.log('❓ Question: What disease does this plant have?');
console.log('\n⏱️  Starting request at:', new Date().toISOString());

const startTime = Date.now();
let responseStarted = false;
let responseCompleted = false;
let fullResponse = '';

const req = https.request(options, (res) => {
  console.log('\n✅ Response started!');
  console.log('📊 Status Code:', res.statusCode);
  console.log('📋 Headers:', JSON.stringify(res.headers, null, 2));

  if (!responseStarted) {
    responseStarted = true;
    const firstByteTime = Date.now() - startTime;
    console.log(`\n⚡ Time to first byte: ${firstByteTime}ms (${(firstByteTime / 1000).toFixed(2)}s)`);
  }

  res.setEncoding('utf8');

  res.on('data', (chunk) => {
    fullResponse += chunk;

    // Parse SSE events
    const lines = chunk.split('\n');
    for (const line of lines) {
      if (line.startsWith('data: ')) {
        try {
          const data = JSON.parse(line.substring(6));
          if (data.type === 'token') {
            process.stdout.write(data.content);
          } else if (data.type === 'done') {
            console.log('\n\n✅ Streaming completed!');
            console.log('📦 Full response data:', JSON.stringify(data, null, 2));
          } else if (data.type === 'error') {
            console.log('\n❌ Error:', data.error);
          }
        } catch (e) {
          // Not valid JSON, skip
        }
      }
    }
  });

  res.on('end', () => {
    if (!responseCompleted) {
      responseCompleted = true;
      const totalTime = Date.now() - startTime;
      console.log(`\n\n⏱️  Total response time: ${totalTime}ms (${(totalTime / 1000).toFixed(2)}s)`);
      console.log(`\n📝 Full raw response length: ${fullResponse.length} bytes`);

      if (totalTime > 180000) {
        console.log('\n⚠️  WARNING: Response took > 3 minutes! This exceeds mobile timeout.');
        console.log('💡 Mobile should show: "We\'re analyzing your plant. We\'ll notify you when ready."');
      } else if (totalTime > 60000) {
        console.log('\n⚠️  Response took > 1 minute. Consider showing notification UI.');
      } else {
        console.log('\n✅ Response time acceptable for real-time streaming.');
      }
    }
  });
});

req.on('error', (error) => {
  console.error('\n❌ Request failed:', error.message);
  const failTime = Date.now() - startTime;
  console.log(`⏱️  Failed after: ${failTime}ms (${(failTime / 1000).toFixed(2)}s)`);
});

req.on('timeout', () => {
  console.error('\n⏱️  Request timeout! (> 180s)');
  req.destroy();
});

// Set timeout to 5 minutes (longer than mobile timeout to see full behavior)
req.setTimeout(300000);

req.write(requestBody);
req.end();

console.log('\n⏳ Waiting for response...\n');
