"""HTML report generator for TKB Automation Framework."""
from __future__ import annotations

import json
import time
from datetime import datetime
from pathlib import Path
from typing import Any

from core.config_loader import config


class ReportGenerator:
    """Generates styled HTML test reports from pytest results."""

    def __init__(self, output_dir: Path | None = None) -> None:
        self.output_dir = output_dir or config.reports_dir / "generated_reports"
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def generate(
        self,
        results: dict[str, Any],
        output_filename: str | None = None,
    ) -> str:
        """Generate an HTML report from test results dict.

        Args:
            results: Dict with 'total', 'passed', 'failed', 'skipped', 'module_results'
            output_filename: Optional output filename.

        Returns:
            Path to the generated HTML report.
        """
        if output_filename is None:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            output_filename = f"report_{ts}.html"

        output_path = self.output_dir / output_filename
        html = self._build_html(results)
        output_path.write_text(html, encoding="utf-8")
        return str(output_path)

    def _build_html(self, results: dict[str, Any]) -> str:
        """Build the full HTML document."""
        total = results.get("total", 0)
        passed = results.get("passed", 0)
        failed = results.get("failed", 0)
        skipped = results.get("skipped", 0)
        pass_rate = (passed / total * 100) if total > 0 else 0
        duration = results.get("duration", "")
        module_results = results.get("module_results", [])

        rows_html = self._build_module_rows(module_results)
        chart_data = self._build_chart_data(module_results)
        table_rows = self._build_detail_rows(results.get("test_details", []))

        return f"""<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TKB Automation - Test Report</title>
<style>
  * {{ margin: 0; padding: 0; box-sizing: border-box; }}
  body {{ font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; color: #1a1a2e; }}
  .container {{ max-width: 1400px; margin: 0 auto; padding: 20px; }}
  .header {{ background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 20px; }}
  .header h1 {{ font-size: 28px; margin-bottom: 8px; }}
  .header .subtitle {{ opacity: 0.8; font-size: 14px; }}
  .summary-cards {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 20px; }}
  .card {{ background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }}
  .card .number {{ font-size: 36px; font-weight: 700; }}
  .card .label {{ font-size: 13px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; }}
  .card.passed .number {{ color: #10b981; }}
  .card.failed .number {{ color: #ef4444; }}
  .card.skipped .number {{ color: #f59e0b; }}
  .card.total .number {{ color: #3b82f6; }}
  .card.rate .number {{ color: #8b5cf6; }}
  .grid-2 {{ display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }}
  .section {{ background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 20px; }}
  .section h2 {{ font-size: 18px; color: #1a1a2e; margin-bottom: 15px; border-bottom: 2px solid #f0f2f5; padding-bottom: 10px; }}
  table {{ width: 100%; border-collapse: collapse; font-size: 14px; }}
  th {{ background: #f8fafc; padding: 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #e2e8f0; }}
  td {{ padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }}
  tr:hover {{ background: #f8fafc; }}
  .badge {{ display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }}
  .badge-passed {{ background: #d1fae5; color: #065f46; }}
  .badge-failed {{ background: #fee2e2; color: #991b1b; }}
  .badge-skipped {{ background: #fef3c7; color: #92400e; }}
  .bar-chart {{ display: flex; align-items: flex-end; gap: 8px; height: 200px; padding: 10px 0; }}
  .bar {{ flex: 1; border-radius: 4px 4px 0 0; text-align: center; font-size: 12px; color: white; display: flex; flex-direction: column; justify-content: flex-end; min-width: 40px; transition: opacity 0.2s; }}
  .bar:hover {{ opacity: 0.85; }}
  .bar-label {{ font-size: 11px; color: #6b7280; text-align: center; margin-top: 5px; word-break: break-word; }}
  .progress-container {{ background: #e5e7eb; border-radius: 20px; height: 12px; overflow: hidden; margin: 10px 0; }}
  .progress-bar {{ height: 100%; border-radius: 20px; transition: width 0.5s ease; }}
  .progress-bar.passed {{ background: linear-gradient(90deg, #10b981, #059669); }}
  .progress-bar.failed {{ background: linear-gradient(90deg, #ef4444, #dc2626); }}
  .error-msg {{ background: #fee2e2; border-left: 4px solid #ef4444; padding: 10px 15px; border-radius: 4px; font-size: 13px; color: #991b1b; margin-top: 5px; }}
  .filter-bar {{ display: flex; gap: 10px; margin-bottom: 15px; flex-wrap: wrap; }}
  .filter-bar button {{ padding: 6px 16px; border: 1px solid #d1d5db; border-radius: 6px; background: white; cursor: pointer; font-size: 13px; }}
  .filter-bar button.active {{ background: #3b82f6; color: white; border-color: #3b82f6; }}
  .test-row.passed {{ background: #f0fdf4; }}
  .test-row.failed {{ background: #fef2f2; }}
  .test-row.skipped {{ background: #fffbeb; }}
  .module-header {{ background: #f1f5f9; padding: 10px; font-weight: 600; cursor: pointer; display: flex; justify-content: space-between; align-items: center; }}
  .module-header:hover {{ background: #e2e8f0; }}
  .chart-wrapper {{ display: flex; gap: 20px; }}
  .chart-wrapper > div {{ flex: 1; }}
  .stats-row {{ display: flex; gap: 15px; margin-bottom: 15px; }}
  .stat-item {{ background: #f8fafc; padding: 10px 15px; border-radius: 6px; flex: 1; text-align: center; }}
  .stat-item .val {{ font-size: 20px; font-weight: 700; }}
  .stat-item .lbl {{ font-size: 12px; color: #6b7280; }}
</style>
</head>
<body>
<div class="container">
  <div class="header">
    <h1>TKB PTIT - Automation Test Report</h1>
    <div class="subtitle">Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} | Duration: {duration}</div>
  </div>

  <div class="summary-cards">
    <div class="card total">
      <div class="number">{total}</div>
      <div class="label">Total Tests</div>
    </div>
    <div class="card passed">
      <div class="number">{passed}</div>
      <div class="label">Passed</div>
    </div>
    <div class="card failed">
      <div class="number">{failed}</div>
      <div class="label">Failed</div>
    </div>
    <div class="card skipped">
      <div class="number">{skipped}</div>
      <div class="label">Skipped</div>
    </div>
    <div class="card rate">
      <div class="number">{pass_rate:.1f}%</div>
      <div class="label">Pass Rate</div>
    </div>
  </div>

  <div class="progress-container">
    <div class="progress-bar passed" style="width: {pass_rate}%"></div>
  </div>

  <div class="chart-wrapper" style="margin-bottom: 20px;">
    <div class="section">
      <h2>Pass Rate by Module</h2>
      <div class="bar-chart" id="barChart">{chart_data['bars_html']}</div>
    </div>
    <div class="section">
      <h2>Results Overview</h2>
      <table>
        <thead>
          <tr><th>Module</th><th>Total</th><th>Passed</th><th>Failed</th><th>Rate</th></tr>
        </thead>
        <tbody>
          {rows_html}
        </tbody>
      </table>
    </div>
  </div>

  <div class="section">
    <h2>Detailed Test Results</h2>
    <div class="filter-bar">
      <button class="active" onclick="filterTests('all')">All</button>
      <button onclick="filterTests('passed')">Passed</button>
      <button onclick="filterTests('failed')">Failed</button>
      <button onclick="filterTests('skipped')">Skipped</button>
    </div>
    <table id="testTable">
      <thead>
        <tr>
          <th>Test ID</th><th>Module</th><th>Description</th><th>Status</th><th>Duration</th><th>Error</th>
        </tr>
      </thead>
      <tbody>
        {table_rows}
      </tbody>
    </table>
  </div>
</div>

<script>
function filterTests(status) {{
  document.querySelectorAll('.filter-bar button').forEach(b => b.classList.remove('active'));
  event.target.classList.add('active');
  document.querySelectorAll('#testTable tbody tr').forEach(row => {{
    if (status === 'all') {{
      row.style.display = '';
    }} else {{
      row.style.display = row.classList.contains(status) ? '' : 'none';
    }}
  }});
}}
</script>
</body>
</html>"""

    def _build_module_rows(self, module_results: list[dict[str, Any]]) -> str:
        """Build table rows for module summary."""
        rows = ""
        for m in module_results:
            name = m.get("module", "Unknown")
            total = m.get("total", 0)
            passed = m.get("passed", 0)
            failed = m.get("failed", 0)
            rate = (passed / total * 100) if total > 0 else 0
            rows += f"""<tr>
  <td>{name}</td>
  <td>{total}</td>
  <td><span class="badge badge-passed">{passed}</span></td>
  <td><span class="badge badge-failed">{failed}</span></td>
  <td><strong>{rate:.1f}%</strong></td>
</tr>"""
        return rows

    def _build_chart_data(self, module_results: list[dict[str, Any]]) -> dict[str, str]:
        """Build chart data for the bar chart."""
        colors = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#ec4899", "#14b8a6"]
        bars = ""
        for i, m in enumerate(module_results):
            total = m.get("total", 0)
            passed = m.get("passed", 0)
            failed = m.get("failed", 0)
            rate = (passed / total * 100) if total > 0 else 0
            color = colors[i % len(colors)]
            height = max(rate, 5)
            name = m.get("module", "")[:12]
            bars += f"""<div>
  <div class="bar" style="height: {height}%; background: {color};">{rate:.0f}%</div>
  <div class="bar-label">{name}<br><small>{passed}/{total}</small></div>
</div>"""
        return {"bars_html": bars}

    def _build_detail_rows(self, test_details: list[dict[str, Any]]) -> str:
        """Build table rows for detailed test results."""
        rows = ""
        for t in test_details:
            status_class = t.get("status", "skipped").lower()
            tc_id = t.get("tc_id", "")
            module = t.get("module", "")
            desc = t.get("description", "")[:80]
            duration = t.get("duration", "")
            error = (t.get("error", "") or "")[:100]
            error_html = f'<span class="error-msg">{error}</span>' if error else ""
            rows += f"""<tr class="test-row {status_class}">
  <td><code>{tc_id}</code></td>
  <td>{module}</td>
  <td title="{desc}">{desc}</td>
  <td><span class="badge badge-{status_class}">{status_class.upper()}</span></td>
  <td>{duration}</td>
  <td>{error_html}</td>
</tr>"""
        if not rows:
            rows = '<tr><td colspan="6" style="text-align:center;color:#9ca3af;">No detailed results available.</td></tr>'
        return rows


def generate_report(results: dict[str, Any], output_filename: str | None = None) -> str:
    """Convenience function to generate a report."""
    generator = ReportGenerator()
    return generator.generate(results, output_filename)
