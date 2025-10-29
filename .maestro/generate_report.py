#!/usr/bin/env python3
"""
Maestro Test Report Generator
Generates comprehensive HTML reports with screenshots and videos
"""

import os
import glob
import sys
from datetime import datetime
from pathlib import Path

def generate_html_report(test_name, screenshots, video_path=None, status="PASSED", duration="Unknown"):
    """Generate a comprehensive HTML report with embedded media"""

    # Convert screenshots to relative paths and sort them
    screenshots_html = ""
    for i, screenshot in enumerate(sorted(screenshots), 1):
        screenshot_name = os.path.basename(screenshot)
        screenshots_html += f"""
        <div class="col-md-4 mb-4">
            <div class="card">
                <img src="../{screenshot_name}" class="card-img-top" alt="{screenshot_name}">
                <div class="card-body">
                    <p class="card-text"><small class="text-muted">Step {i}: {screenshot_name}</small></p>
                </div>
            </div>
        </div>
        """

    # Video embed if available
    video_html = ""
    if video_path and os.path.exists(video_path):
        video_name = os.path.basename(video_path)
        video_html = f"""
        <div class="mb-5">
            <h3>📹 Test Recording</h3>
            <div class="ratio ratio-16x9">
                <video controls>
                    <source src="../{video_name}" type="video/mp4">
                    Your browser does not support the video tag.
                </video>
            </div>
        </div>
        """

    status_class = "success" if status == "PASSED" else "danger"
    status_icon = "✅" if status == "PASSED" else "❌"

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{test_name} - Test Report</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {{
            background-color: #f8f9fa;
            padding: 20px;
        }}
        .header-banner {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 0;
            margin-bottom: 30px;
            border-radius: 10px;
        }}
        .status-badge {{
            font-size: 2rem;
            padding: 15px 30px;
            border-radius: 50px;
        }}
        .screenshot-section {{
            background: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        .card {{
            transition: transform 0.2s;
        }}
        .card:hover {{
            transform: scale(1.05);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }}
        .stats-card {{
            background: white;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
    </style>
</head>
<body>
    <div class="container-fluid">
        <!-- Header Banner -->
        <div class="header-banner text-center">
            <h1 class="display-4">🌾 Nông Trí App - Test Report</h1>
            <p class="lead">{test_name}</p>
            <span class="badge status-badge bg-{status_class}">{status_icon} {status}</span>
        </div>

        <!-- Test Statistics -->
        <div class="row mb-4">
            <div class="col-md-4">
                <div class="stats-card">
                    <h5>Test Status</h5>
                    <p class="h3 text-{status_class}">{status}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stats-card">
                    <h5>Duration</h5>
                    <p class="h3">{duration}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stats-card">
                    <h5>Screenshots Captured</h5>
                    <p class="h3">{len(screenshots)}</p>
                </div>
            </div>
        </div>

        <!-- Video Recording Section -->
        {video_html}

        <!-- Screenshots Gallery -->
        <div class="screenshot-section">
            <h2 class="mb-4">📸 Test Execution Screenshots</h2>
            <div class="row">
                {screenshots_html}
            </div>
        </div>

        <!-- Footer -->
        <div class="text-center text-muted mt-5">
            <p>Generated on {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
            <p>Maestro Test Framework | Nông Trí Agricultural AI Assistant</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
"""
    return html


def main():
    if len(sys.argv) < 2:
        print("Usage: generate_report.py <test_name> [status] [duration]")
        sys.exit(1)

    test_name = sys.argv[1]
    status = sys.argv[2] if len(sys.argv) > 2 else "PASSED"
    duration = sys.argv[3] if len(sys.argv) > 3 else "Unknown"

    # Get the maestro directory
    maestro_dir = Path(__file__).parent
    reports_dir = maestro_dir / "reports"
    reports_dir.mkdir(exist_ok=True)

    # Find screenshots for this test
    screenshot_patterns = [
        maestro_dir / f"{i:02d}_*.png" for i in range(1, 20)
    ]

    screenshots = []
    for pattern in screenshot_patterns:
        screenshots.extend(glob.glob(str(pattern)))

    # Look for video file
    video_path = maestro_dir / f"{test_name}.mp4"
    if not video_path.exists():
        # Try without test name prefix
        video_candidates = list(maestro_dir.glob("*.mp4"))
        video_path = video_candidates[0] if video_candidates else None

    # Generate HTML
    html_content = generate_html_report(
        test_name,
        screenshots,
        str(video_path) if video_path else None,
        status,
        duration
    )

    # Save report
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    report_path = reports_dir / f"{test_name}_{timestamp}.html"

    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(html_content)

    print(f"✅ Report generated: {report_path}")
    print(f"   - Screenshots: {len(screenshots)}")
    print(f"   - Video: {'Yes' if video_path else 'No'}")
    print(f"\nTo view: open {report_path}")

    return str(report_path)


if __name__ == "__main__":
    main()
