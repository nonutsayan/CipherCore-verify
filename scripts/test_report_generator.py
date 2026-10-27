#!/usr/bin/env python3
"""
Test Suite for backend/report_generator.py
Validates CLI options, programmatic API, PDF byte integrity, JSON schema, and edge cases.
"""

import os
import sys
import json
import subprocess
import tempfile

WORKSPACE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GENERATOR_SCRIPT = os.path.join(WORKSPACE, "backend", "report_generator.py")

def run_test(name, fn):
    try:
        fn()
        print(f"[PASS] {name}")
        return True
    except AssertionError as e:
        print(f"[FAIL] {name}: Assertion failed: {e}")
        return False
    except Exception as e:
        print(f"[FAIL] {name}: Unexpected error: {e}")
        return False

def test_cli_standard_pdf():
    with tempfile.TemporaryDirectory() as tmpdir:
        out_pdf = os.path.join(tmpdir, "test_101.pdf")
        cmd = [sys.executable, GENERATOR_SCRIPT, "--app-id", "APP-2026-101", "--format", "pdf", "--output", out_pdf]
        res = subprocess.run(cmd, capture_output=True, text=True)
        assert res.returncode == 0, f"Process exited with {res.returncode}: {res.stderr}"
        assert os.path.exists(out_pdf), "PDF file was not created"
        assert os.path.getsize(out_pdf) > 2000, f"PDF file size too small: {os.path.getsize(out_pdf)}"
        with open(out_pdf, "rb") as f:
            header = f.read(8)
            assert header.startswith(b"%PDF-"), f"Invalid PDF magic bytes: {header}"

def test_cli_standard_json():
    with tempfile.TemporaryDirectory() as tmpdir:
        out_json = os.path.join(tmpdir, "test_101.json")
        cmd = [sys.executable, GENERATOR_SCRIPT, "--app-id", "APP-2026-101", "--format", "json", "--output", out_json]
        res = subprocess.run(cmd, capture_output=True, text=True)
        assert res.returncode == 0, f"Process exited with {res.returncode}: {res.stderr}"
        assert os.path.exists(out_json), "JSON file was not created"
        with open(out_json, "r", encoding="utf-8") as f:
            data = json.load(f)
            assert data["applicationProfile"]["applicationId"] == "APP-2026-101"
            assert data["applicationProfile"]["institutionName"] == "Apex Institute of Technology"
            assert len(data["documentAuditMatrix"]) == 6, "Expected 6 mandatory documents"
            assert "computerVisionAudit" in data
            assert "forensicAnalysis" in data
            assert "blockchainProofOfAuthority" in data

def test_custom_data_injection():
    with tempfile.TemporaryDirectory() as tmpdir:
        custom_payload = {
            "id": "APP-2026-999",
            "institution": "Autonomous Test Institute of Technology",
            "status": "Verified",
            "decision": "Approved for 3 Academic Years",
            "overallScore": 98.2
        }
        out_pdf = os.path.join(tmpdir, "custom.pdf")
        out_json = os.path.join(tmpdir, "custom.json")
        
        # Test CLI with --data json string
        cmd = [
            sys.executable, GENERATOR_SCRIPT,
            "--app-id", "APP-2026-999",
            "--data", json.dumps(custom_payload),
            "--format", "both",
            "--output", os.path.join(tmpdir, "custom")
        ]
        res = subprocess.run(cmd, capture_output=True, text=True)
        assert res.returncode == 0, f"Process exited with {res.returncode}: {res.stderr}"
        assert os.path.exists(out_pdf), "Custom PDF not generated"
        assert os.path.exists(out_json), "Custom JSON not generated"
        with open(out_json, "r", encoding="utf-8") as f:
            data = json.load(f)
            assert data["applicationProfile"]["applicationId"] == "APP-2026-999"
            assert data["applicationProfile"]["institutionName"] == "Autonomous Test Institute of Technology"
            assert data["scrutinyExecutiveSummary"]["overallScore"] == 98.2

def test_import_as_python_module():
    sys.path.insert(0, os.path.join(WORKSPACE, "backend"))
    import report_generator
    with tempfile.TemporaryDirectory() as tmpdir:
        data = report_generator.get_application_data("APP-2026-102")
        pdf_path = os.path.join(tmpdir, "direct_module.pdf")
        report_generator.generate_pdf_report(data, pdf_path)
        assert os.path.exists(pdf_path), "Module direct call failed to produce PDF"
        assert os.path.getsize(pdf_path) > 3000, "Module direct call produced empty PDF"

def main():
    print(f"Executing Report Generator Test Suite against: {GENERATOR_SCRIPT}")
    results = [
        run_test("CLI Standard PDF Generation", test_cli_standard_pdf),
        run_test("CLI Standard JSON Generation", test_cli_standard_json),
        run_test("Custom Data Injection via JSON CLI", test_custom_data_injection),
        run_test("Direct Python Module Import and Execution", test_import_as_python_module),
    ]

    passed = sum(1 for r in results if r)
    total = len(results)
    print(f"\nReport Generator Test Summary: {passed}/{total} Passed")
    if passed != total:
        sys.exit(1)

if __name__ == "__main__":
    main()
