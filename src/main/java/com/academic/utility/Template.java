package com.academic.utility;

public class Template {

    public static final String TERM_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
                @page {
                    size: A4;
                    margin: 0.5cm;
                }
                body { 
                    font-family: 'Times New Roman', Times, serif; 
                    font-size: 11px; 
                    margin: 0; 
                    padding: 15px; 
                    color: #000; 
                    height: 100%;
                }
                .page-border {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    border: 1px solid #000;
                    z-index: -1;
                }
                .report-container { 
                    position: relative;
                    min-height: 1000px;
                }
                
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th, td { border: 1px solid #333; padding: 8px 5px; text-align: center; }
                
                .header-top { border: none !important; margin-bottom: 5px; }
                .header-top td { border: none !important; font-size: 10px; padding: 0; }
                
                .main-header { border: none !important; margin-bottom: 15px; }
                .main-header td { border: none !important; vertical-align: middle; }
                .school-name { font-size: 28px; font-weight: 900; color: #0b3d91; text-align: center; font-family: 'Arial Black', Gadget, sans-serif; }
                .logo-img { height: 75px; }
                
                .report-title { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 1px; }
                .session-title { text-align: center; font-size: 12px; margin-bottom: 15px; }
                
                .student-info { border: none !important; margin-bottom: 20px; width: 100%; table-layout: fixed; }
                .student-info td { border: none !important; text-align: left; padding: 4px 0; vertical-align: bottom; }
                .student-info .label { width: 110px; font-size: 11px; }
                .student-info .colon { width: 15px; text-align: center; }
                .student-info .value { border-bottom: 1px solid #333 !important; font-weight: bold; min-width: 180px; padding-bottom: 2px; text-transform: uppercase; }
                .student-info .gap { width: 30px; }
                
                .section-header { background-color: #d1d5db; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; }
                .sub-header { background-color: #f3f4f6; font-size: 10px; }
                
                .summary-container { border: none !important; margin-top: 20px; }
                .summary-container td { border: none !important; padding: 0 10px; }
                .summary-box { border: 2px solid #333 !important; padding: 12px; font-weight: bold; text-align: center; }
                .summary-label { font-size: 10px; margin-bottom: 5px; color: #555; }
                .summary-value { font-size: 14px; }
                
                .footer-wrapper {
                    position: absolute;
                    bottom: 25px;
                    left: 0;
                    right: 0;
                    padding: 0 15px;
                }
                .footer-table { border: none !important; width: 100%; border-collapse: collapse; }
                .footer-table td { border: none !important; vertical-align: bottom; font-weight: 500; font-size: 11px; padding: 0; }
                
                .promoted-label { font-size: 12px; text-align: left; padding-bottom: 12px !important; }
                .promoted-underline { border-bottom: 1px solid #000; min-width: 150px; display: inline-block; padding: 0 5px; font-weight: bold; }

                .instructions { text-align: center; margin-top: 35px; }
                .instructions-title { font-weight: bold; margin-bottom: 5px; font-size: 11px; }
                .grade-range { font-size: 10px; color: #000; }
            </style>
            </head>
            <body>
            <div class="page-border"></div>
            <div class="report-container">
                <table class="header-top">
                    <tr>
                        <td align="left">AFFILIATION NO. 1030238</td>
                        <td align="right">SCHOOL CODE: 50214</td>
                    </tr>
                </table>
                
                <table class="main-header">
                    <tr>
                        <td width="15%"><img src="${LEFT_LOGO}" class="logo-img"/></td>
                        <td width="70%" class="school-name">PROGRESSIVE PUBLIC SCHOOL (PPS)</td>
                        <td width="15%" align="right"><img src="${RIGHT_LOGO}" class="logo-img"/></td>
                    </tr>
                </table>
                
                <div class="report-title">${REPORT_TITLE}</div>
                <div class="session-title">(Academic Session ${SESSION})</div>
                
                <table class="student-info">
                    <tr>
                        <td class="label">ADMISSION NO</td><td class="colon">:</td><td class="value">${ADMISSION_NO}</td>
                        <td class="gap"></td>
                        <td class="label">ROLL NO.</td><td class="colon">:</td><td class="value">${ROLL_NO}</td>
                    </tr>
                    <tr>
                        <td class="label">STUDENT NAME</td><td class="colon">:</td><td class="value">${STUDENT_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">CLASS</td><td class="colon">:</td><td class="value">${CLASS}</td>
                    </tr>
                    <tr>
                        <td class="label">FATHER'S NAME</td><td class="colon">:</td><td class="value">${FATHER_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">DATE OF BIRTH</td><td class="colon">:</td><td class="value">${DOB}</td>
                    </tr>
                    <tr>
                        <td class="label">MOTHER'S NAME</td><td class="colon">:</td><td class="value">${MOTHER_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">SECTION</td><td class="colon">:</td><td class="value">${SECTION}</td>
                    </tr>
                </table>
                
                <table>
                    <thead>
                        <tr>
                            <th colspan="7" class="section-header">SCHOLASTIC AREA</th>
                        </tr>
                        <tr class="sub-header">
                            <th style="width: 200px;">SUBJECTS</th>
                            <th>PT</th>
                            <th>NB</th>
                            <th>SE</th>
                            <th>TERM</th>
                            <th style="background-color: #e5e7eb;">MARKS OBTAINED</th>
                            <th style="width: 80px;">GRADE</th>
                        </tr>
                        <tr class="sub-header" style="font-size: 8px;">
                            <th></th>
                            <th>(10)</th>
                            <th>(5)</th>
                            <th>(5)</th>
                            <th>(80)</th>
                            <th style="background-color: #e5e7eb;">(100)</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        ${SUBJECT_ROWS}
                    </tbody>
                </table>
                
                <table class="summary-container">
                    <tr>
                        <td width="33%">
                            <div class="summary-box">
                                <div class="summary-label">OVERALL MARKS</div>
                                <div class="summary-value">${TOTAL_MARKS} / ${TOTAL_MAX}</div>
                            </div>
                        </td>
                        <td width="34%">
                            <div class="summary-box">
                                <div class="summary-label">PERCENT (%)</div>
                                <div class="summary-value">${PERCENTAGE}</div>
                            </div>
                        </td>
                        <td width="33%">
                            <div class="summary-box">
                                <div class="summary-label">GRADE</div>
                                <div class="summary-value">${GRADE}</div>
                            </div>
                        </td>
                    </tr>
                </table>
                
                <table style="margin-top: 15px;">
                    <thead>
                        <tr>
                            <th colspan="2" class="section-header">CO-SCHOLASTIC AREA</th>
                        </tr>
                        <tr class="sub-header">
                            <th style="width: 70%;">ACTIVITY</th>
                            <th style="width: 30%;">GRADE</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${ACTIVITY_ROWS}
                    </tbody>
                </table>
                
                <div class="footer-wrapper">
                    <table class="footer-table">
                        <tr>
                            <td colspan="4" class="promoted-label">
                                Promoted to Class : <span class="promoted-underline">${PROMOTED_TO}</span>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" style="text-align: left;">Date : ${DATE}</td>
                            <td width="25%" style="text-align: center;">School Stamp</td>
                            <td width="25%" style="text-align: center;">Class Teacher</td>
                            <td width="25%" style="text-align: right;">Principal</td>
                        </tr>
                    </table>

                    <div class="instructions">
                        <div class="instructions-title">INSTRUCTIONS</div>
                        <div class="grade-range">Grade Range: [91-100=A1], [81-90=A2], [71-80=B1], [61-70=B2], [51-60=C1], [41-50=C2], [33-40=D], 32 &amp; Below=E]</div>
                        <div class="grade-range" style="margin-top: 3px;">Abbreviation : [PT-Periodic Test], [NB-Note Book], [SE-Subject Enrichment], [ABS-Absent]</div>
                    </div>
                </div>
            </div>
            </body>
            </html>
            """;


    public static final String ANNUAL_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
                @page {
                    size: A4;
                    margin: 0.5cm;
                }
                body { 
                    font-family: 'Times New Roman', Times, serif; 
                    font-size: 10px; 
                    margin: 0; 
                    padding: 15px; 
                    color: #000;
                    height: 100%;
                }
                .page-border {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    border: 1px solid #000;
                    z-index: -1;
                }
                .report-container { 
                    position: relative;
                    min-height: 1000px;
                }
                
                table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                th, td { border: 1px solid #333; padding: 4px 2px; text-align: center; }
                
                .header-top { border: none !important; margin-bottom: 5px; }
                .header-top td { border: none !important; font-size: 9px; padding: 0; }
                
                .main-header { border: none !important; margin-bottom: 8px; }
                .main-header td { border: none !important; vertical-align: middle; }
                .school-name { font-size: 24px; font-weight: 900; color: #0b3d91; text-align: center; font-family: 'Arial Black', Gadget, sans-serif; }
                .logo-img { height: 60px; }
                
                .report-title { text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 2px; text-transform: uppercase; letter-spacing: 1px; }
                .session-title { text-align: center; font-size: 11px; margin-bottom: 10px; }
                
                .student-info { border: none !important; margin-bottom: 12px; width: 100%; table-layout: fixed; }
                .student-info td { border: none !important; text-align: left; padding: 2px 0; vertical-align: bottom; }
                .student-info .label { width: 110px; font-size: 10px; }
                .student-info .colon { width: 12px; text-align: center; }
                .student-info .value { border-bottom: 1px solid #333 !important; font-weight: bold; min-width: 150px; padding-bottom: 2px; text-transform: uppercase; }
                .student-info .gap { width: 25px; }
                
                .section-header { background-color: #d1d5db; font-weight: bold; text-transform: uppercase; letter-spacing: 0.5px; font-size: 10px; }
                .sub-header { background-color: #f3f4f6; font-size: 9px; }
                
                .summary-container { border: none !important; margin-top: 8px; }
                .summary-container td { border: none !important; padding: 0 8px; }
                .summary-box { border: 2px solid #333 !important; padding: 6px; font-weight: bold; text-align: center; }
                .summary-label { font-size: 9px; margin-bottom: 2px; color: #555; }
                .summary-value { font-size: 11px; }
                
                .footer-wrapper {
                    position: absolute;
                    bottom: 25px;
                    left: 0;
                    right: 0;
                    padding: 0 15px;
                }
                .footer-table { border: none !important; width: 100%; border-collapse: collapse; }
                .footer-table td { border: none !important; vertical-align: bottom; font-weight: 500; font-size: 11px; padding: 0; }
                
                .promoted-label { font-size: 12px; text-align: left; padding-bottom: 12px !important; }
                .promoted-underline { border-bottom: 1px solid #000; min-width: 150px; display: inline-block; padding: 0 5px; font-weight: bold; }

                .instructions { text-align: center; margin-top: 35px; }
                .instructions-title { font-weight: bold; margin-bottom: 5px; font-size: 11px; }
                .grade-range { font-size: 10px; color: #000; }
            </style>
            </head>
            <body>
            <div class="page-border"></div>
            <div class="report-container">
                <table class="header-top">
                    <tr>
                        <td align="left">AFFILIATION NO. 1030238</td>
                        <td align="right">SCHOOL CODE: 50214</td>
                    </tr>
                </table>
                
                <table class="main-header">
                    <tr>
                        <td width="15%"><img src="${LEFT_LOGO}" class="logo-img"/></td>
                        <td width="70%" class="school-name">PROGRESSIVE PUBLIC SCHOOL (PPS)</td>
                        <td width="15%" align="right"><img src="${RIGHT_LOGO}" class="logo-img"/></td>
                    </tr>
                </table>
                
                <div class="report-title">ANNUAL PROGRESS REPORT</div>
                <div class="session-title">(Academic Session ${SESSION})</div>
                
                <table class="student-info">
                    <tr>
                        <td class="label">ADMISSION NO</td><td class="colon">:</td><td class="value">${ADMISSION_NO}</td>
                        <td class="gap"></td>
                        <td class="label">ROLL NO.</td><td class="colon">:</td><td class="value">${ROLL_NO}</td>
                    </tr>
                    <tr>
                        <td class="label">STUDENT NAME</td><td class="colon">:</td><td class="value">${STUDENT_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">CLASS</td><td class="colon">:</td><td class="value">${CLASS}</td>
                    </tr>
                    <tr>
                        <td class="label">FATHER'S NAME</td><td class="colon">:</td><td class="value">${FATHER_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">DATE OF BIRTH</td><td class="colon">:</td><td class="value">${DOB}</td>
                    </tr>
                    <tr>
                        <td class="label">MOTHER'S NAME</td><td class="colon">:</td><td class="value">${MOTHER_NAME}</td>
                        <td class="gap"></td>
                        <td class="label">SECTION</td><td class="colon">:</td><td class="value">${SECTION}</td>
                    </tr>
                </table>
                
                <table>
                    <thead>
                        <tr>
                            <th colspan="15" class="section-header">SCHOLASTIC AREA</th>
                        </tr>
                        <tr>
                            <th rowspan="3" class="section-header" style="width: 140px;">SUBJECTS</th>
                            <th colspan="6" class="section-header">TERM - 1 (100)</th>
                            <th colspan="6" class="section-header">TERM - 2 (100)</th>
                            <th colspan="2" class="section-header">OVERALL</th>
                        </tr>
                        <tr class="sub-header">
                            <th rowspan="2">PT<br/>(10)</th><th rowspan="2">NB<br/>(5)</th><th rowspan="2">SE<br/>(5)</th><th rowspan="2">TERM 1<br/>(80)</th><th rowspan="2" style="background-color:#e5e7eb">MARKS OBTAINED<br/>(100)</th><th rowspan="2">GRADE</th>
                            <th rowspan="2">PT<br/>(10)</th><th rowspan="2">NB<br/>(5)</th><th rowspan="2">SE<br/>(5)</th><th rowspan="2">TERM 2<br/>(80)</th><th rowspan="2" style="background-color:#e5e7eb">MARKS OBTAINED<br/>(100)</th><th rowspan="2">GRADE</th>
                            <th style="font-size: 7px;">T1(50%)+T2(50%)</th><th rowspan="2">GRADE</th>
                        </tr>
                        <tr class="sub-header">
                            <th>MARKS OBTAINED<br/>(100)</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${SUBJECT_ROWS}
                    </tbody>
                </table>
                
                <table class="summary-container">
                    <tr>
                        <td width="33%">
                            <div class="summary-box">
                                <div class="summary-label">OVERALL MARKS OBTAINED</div>
                                <div class="summary-value">${TOTAL_MARKS} / ${TOTAL_MAX}</div>
                            </div>
                        </td>
                        <td width="34%">
                            <div class="summary-box">
                                <div class="summary-label">PERCENT (%)</div>
                                <div class="summary-value">${PERCENTAGE}</div>
                            </div>
                        </td>
                        <td width="33%">
                            <div class="summary-box">
                                <div class="summary-label">OVERALL GRADE</div>
                                <div class="summary-value">${GRADE}</div>
                            </div>
                        </td>
                    </tr>
                </table>
                
                <table style="margin-top: 10px;">
                    <thead>
                        <tr>
                            <th colspan="4" class="section-header">CO-SCHOLASTIC AREA</th>
                        </tr>
                        <tr class="sub-header" style="font-weight: bold;">
                            <th style="width: 35%;">TERM-1<br/>(Grades are awarded on 3-point [A-C] Scale)</th>
                            <th style="width: 15%;">GRADE</th>
                            <th style="width: 35%;">TERM-2<br/>(Grades are awarded on 3-point [A-C] Scale)</th>
                            <th style="width: 15%;">GRADE</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${ACTIVITY_ROWS}
                    </tbody>
                </table>
                
                <div class="footer-wrapper">
                    <table class="footer-table">
                        <tr>
                            <td colspan="4" class="promoted-label">
                                Promoted to Class : <span class="promoted-underline">${PROMOTED_TO}</span>
                            </td>
                        </tr>
                        <tr>
                            <td width="25%" style="text-align: left;">Date : ${DATE}</td>
                            <td width="25%" style="text-align: center;">School Stamp</td>
                            <td width="25%" style="text-align: center;">Class Teacher</td>
                            <td width="25%" style="text-align: right;">Principal</td>
                        </tr>
                    </table>

                    <div class="instructions">
                        <div class="instructions-title">INSTRUCTIONS</div>
                        <div class="grade-range">Grade Range: [91-100=A1], [81-90=A2], [71-80=B1], [61-70=B2], [51-60=C1], [41-50=C2], [33-40=D], 32 &amp; Below=E]</div>
                        <div class="grade-range" style="margin-top: 3px;">Abbreviation : [PT-Periodic Test], [NB-Note Book], [SE-Subject Enrichment], [ABS-Absent]</div>
                    </div>
                </div>
            </div>
            </body>
            </html>
            """;
}