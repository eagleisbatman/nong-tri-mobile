#!/bin/bash
# Maestro Test Runner with Video Recording & HTML Reports
# This script runs Maestro tests and generates comprehensive HTML reports

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Nông Trí App - Maestro Test Suite with Video Recording  ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Add Maestro to PATH if not already there
export PATH="$PATH:$HOME/.maestro/bin"

# Create reports directory
REPORTS_DIR="$(pwd)/reports"
mkdir -p "$REPORTS_DIR"

# Get timestamp for reports
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Function to run a single test with HTML report
run_test() {
    local test_file=$1
    local test_name=$(basename "$test_file" .yaml)
    local output_file="$REPORTS_DIR/${test_name}_${TIMESTAMP}.html"

    echo -e "${BLUE}▶ Running: $test_name${NC}"
    echo "  Output: $output_file"

    if maestro test --format html --output "$output_file" "$test_file"; then
        echo -e "${GREEN}✓ $test_name PASSED${NC}"
        echo "  Report: $output_file"
        return 0
    else
        echo -e "${RED}✗ $test_name FAILED${NC}"
        echo "  Report: $output_file"
        return 1
    fi
}

# Function to run full test suite
run_suite() {
    local suite_name=$1
    local output_file="$REPORTS_DIR/${suite_name}_${TIMESTAMP}.html"

    echo -e "${BLUE}▶ Running: $suite_name (Full Suite)${NC}"
    echo "  Output: $output_file"

    if maestro test --format html --output "$output_file" "$(pwd)"; then
        echo -e "${GREEN}✓ Full Suite PASSED${NC}"
        echo "  Report: $output_file"
        return 0
    else
        echo -e "${RED}✗ Full Suite FAILED${NC}"
        echo "  Report: $output_file"
        return 1
    fi
}

# Parse command line arguments
case "${1:-all}" in
    "01"|"basic")
        run_test "$(pwd)/01_basic_chat_flow.yaml"
        ;;
    "02"|"agricultural")
        run_test "$(pwd)/02_agricultural_query.yaml"
        ;;
    "03"|"streaming")
        run_test "$(pwd)/03_streaming_test.yaml"
        ;;
    "04"|"autoscroll")
        run_test "$(pwd)/04_auto_scroll_test.yaml"
        ;;
    "05"|"language")
        run_test "$(pwd)/05_language_test.yaml"
        ;;
    "smoke")
        echo -e "${BLUE}Running Smoke Tests...${NC}"
        FAILED=0
        run_test "$(pwd)/01_basic_chat_flow.yaml" || FAILED=$((FAILED + 1))
        run_test "$(pwd)/02_agricultural_query.yaml" || FAILED=$((FAILED + 1))

        if [ $FAILED -eq 0 ]; then
            echo -e "${GREEN}✓ All Smoke Tests PASSED${NC}"
        else
            echo -e "${RED}✗ $FAILED Smoke Tests FAILED${NC}"
            exit 1
        fi
        ;;
    "all")
        echo -e "${BLUE}Running All Tests...${NC}"
        run_suite "full_test_suite"
        ;;
    "help"|"-h"|"--help")
        echo "Usage: ./run_tests.sh [test]"
        echo ""
        echo "Tests:"
        echo "  01, basic       - Basic chat flow test"
        echo "  02, agricultural - Agricultural query test"
        echo "  03, streaming   - Streaming behavior test"
        echo "  04, autoscroll  - Auto-scroll functionality test"
        echo "  05, language    - Vietnamese language test"
        echo "  smoke           - Run smoke tests (01 + 02)"
        echo "  all (default)   - Run all tests"
        echo ""
        echo "Reports are saved to: $REPORTS_DIR"
        exit 0
        ;;
    *)
        echo -e "${RED}Unknown test: $1${NC}"
        echo "Run './run_tests.sh help' for usage"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Test execution complete!                ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
echo ""
echo "Reports location: $REPORTS_DIR"
echo ""
echo "To view the latest report:"
echo "  open $REPORTS_DIR/*_${TIMESTAMP}.html"
