#!/usr/bin/env python
"""Main test runner for TKB Automation Framework.

Usage:
    python run_tests.py --module rooms
    python run_tests.py --module all --parallel 4
    python run_tests.py --module all --html
    python run_tests.py --module all --generate-report
"""
from __future__ import annotations

import argparse
import os
import sys
from datetime import datetime
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent
sys.path.insert(0, str(PROJECT_ROOT))


def parse_args():
    parser = argparse.ArgumentParser(description="TKB Automation Framework Test Runner")
    parser.add_argument("--module", "-m", default="all",
                        choices=["all", "rooms", "semesters", "scheduling",
                                 "ctdt", "users", "tkb", "hvk"],
                        help="Test module to run (default: all)")
    parser.add_argument("--browser", "-b", default="chrome",
                        choices=["chrome", "firefox", "edge"],
                        help="Browser (default: chrome)")
    parser.add_argument("--parallel", "-p", type=int, default=1,
                        help="Parallel workers (default: 1)")
    parser.add_argument("--headless", action="store_true", help="Run headless")
    parser.add_argument("--html", action="store_true", help="Generate HTML report")
    parser.add_argument("--generate-report", action="store_true",
                        help="Generate custom HTML report")
    parser.add_argument("--markers", "-k", default="",
                        help="pytest -k expression")
    parser.add_argument("--dry-run", action="store_true",
                        help="Collect tests without running")
    parser.add_argument("--verbose", "-v", action="count", default=0)
    return parser.parse_args()


def build_pytest_args(args):
    pytest_args = []
    module_files = {
        "rooms": "tests/test_rooms.py",
        "semesters": "tests/test_semesters.py",
        "scheduling": "tests/test_scheduling.py",
        "ctdt": "tests/test_ctdt.py",
        "users": "tests/test_users.py",
        "tkb": "tests/test_tkb.py",
        "hvk": "tests/test_hvk.py",
    }
    if args.module == "all":
        test_file = "tests/"
    else:
        test_file = module_files.get(args.module, "tests/")

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    if args.html:
        report_dir = PROJECT_ROOT / "reports" / "generated_reports"
        report_dir.mkdir(parents=True, exist_ok=True)
        pytest_args.append(f"--html={report_dir}/report_{timestamp}.html")
        pytest_args.append("--self-contained-html")

    if args.verbose == 1:
        pytest_args.append("-v")
    elif args.verbose >= 2:
        pytest_args.append("-vv")

    if args.markers:
        pytest_args.extend(["-k", args.markers])

    if args.parallel > 1:
        pytest_args.append(f"-n{args.parallel}")
    elif args.parallel == -1:
        try:
            import multiprocessing
            workers = max(1, multiprocessing.cpu_count() - 1)
            pytest_args.append(f"-n{workers}")
        except Exception:
            pass

    if args.dry_run:
        pytest_args.append("--collect-only")

    pytest_args.extend(["-c", "pytest.ini"])
    pytest_args.append(test_file)
    return pytest_args


def set_env(args):
    os.environ["BROWSER"] = args.browser
    if args.headless:
        os.environ["HEADLESS"] = "true"
    os.environ["PYTHONDONTWRITEBYTECODE"] = "1"


def main():
    args = parse_args()
    set_env(args)

    (PROJECT_ROOT / "reports" / "generated_reports").mkdir(parents=True, exist_ok=True)
    (PROJECT_ROOT / "reports" / "allure-results").mkdir(parents=True, exist_ok=True)
    (PROJECT_ROOT / "logs").mkdir(parents=True, exist_ok=True)

    pytest_args = build_pytest_args(args)

    print("=" * 70)
    print("  TKB PTIT - Selenium Automation Test Runner")
    print(f"  Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  Module: {args.module.upper()}")
    print(f"  Browser: {args.browser}")
    print(f"  Headless: {args.headless}")
    print(f"  Parallel: {args.parallel}")
    print("=" * 70)
    print(f"\nRunning: pytest {' '.join(pytest_args)}\n")

    import pytest
    exit_code = pytest.main(pytest_args)

    if args.generate_report:
        print("\nGenerating custom HTML report...")
        try:
            from core.report_generator import ReportGenerator
            rep = ReportGenerator()
            result = {
                "total": 566,
                "passed": 0,
                "failed": 0,
                "skipped": 0,
                "module_results": [],
                "test_details": [],
            }
            rep.generate(result, f"report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.html")
            print("Custom report generated in reports/generated_reports/")
        except Exception as e:
            print(f"Report generation skipped: {e}")

    print("\n" + "=" * 70)
    print("  Test Run Complete")
    print(f"  Reports: {PROJECT_ROOT / 'reports'}")
    print("=" * 70)
    return exit_code


if __name__ == "__main__":
    sys.exit(main())
