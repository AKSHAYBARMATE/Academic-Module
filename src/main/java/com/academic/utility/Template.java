package com.academic.utility;

public class Template {

    public static final String TERM_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
                body { font-family: 'Inter', Arial, sans-serif; font-size: 11px; margin: 0; padding: 20px; color: #333; }
                .report-container { border: 2px solid #333; padding: 20px; }
                
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
                .student-info .value { border-bottom: 1px solid #333 !important; font-weight: bold; min-width: 180px; padding-bottom: 2px; }
                .student-info .gap { width: 30px; }
                
                .section-header { background-color: #d1d5db; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; }
                .sub-header { background-color: #f3f4f6; font-size: 10px; }
                
                .summary-container { border: none !important; margin-top: 20px; }
                .summary-container td { border: none !important; padding: 0 10px; }
                .summary-box { border: 2px solid #333 !important; padding: 12px; font-weight: bold; text-align: center; }
                .summary-label { font-size: 10px; margin-bottom: 5px; color: #555; }
                .summary-value { font-size: 14px; }
                
                .footer-table { border: none !important; margin-top: 50px; }
                .footer-table td { border: none !important; text-align: center; vertical-align: bottom; }
                .footer-line { border-top: 1px solid #333; width: 150px; margin: 0 auto 5px; }
            </style>
            </head>
            <body>
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
                
                <table class="footer-table">
                    <tr>
                        <td width="25%">Date : ${DATE}</td>
                        <td width="25%">School Stamp</td>
                        <td width="25%">Class Teacher</td>
                        <td width="25%">Principal</td>
                    </tr>
                </table>
            </div>
            </body>
            </html>
            """;


    public static final String ANNUAL_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
                body { font-family: 'Inter', Arial, sans-serif; font-size: 10px; margin: 0; padding: 20px; color: #333; }
                .report-container { border: 2px solid #333; padding: 20px; }
                
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th, td { border: 1px solid #333; padding: 5px 3px; text-align: center; }
                
                .header-top { border: none !important; margin-bottom: 5px; }
                .header-top td { border: none !important; font-size: 9px; padding: 0; }
                
                .main-header { border: none !important; margin-bottom: 10px; }
                .main-header td { border: none !important; vertical-align: middle; }
                .school-name { font-size: 24px; font-weight: 900; color: #0b3d91; text-align: center; font-family: 'Arial Black', Gadget, sans-serif; }
                .logo-img { height: 65px; }
                
                .report-title { text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 2px; text-transform: uppercase; letter-spacing: 1px; }
                .session-title { text-align: center; font-size: 11px; margin-bottom: 15px; }
                
                .student-info { border: none !important; margin-bottom: 20px; width: 100%; table-layout: fixed; }
                .student-info td { border: none !important; text-align: left; padding: 3px 0; vertical-align: bottom; }
                .student-info .label { width: 110px; font-size: 10px; }
                .student-info .colon { width: 12px; text-align: center; }
                .student-info .value { border-bottom: 1px solid #333 !important; font-weight: bold; min-width: 150px; padding-bottom: 2px; text-transform: uppercase; }
                .student-info .gap { width: 25px; }
                
                .section-header { background-color: #d1d5db; font-weight: bold; text-transform: uppercase; letter-spacing: 0.5px; font-size: 10px; }
                .sub-header { background-color: #f3f4f6; font-size: 9px; }
                
                .summary-container { border: none !important; margin-top: 15px; }
                .summary-container td { border: none !important; padding: 0 8px; }
                .summary-box { border: 2px solid #333 !important; padding: 10px; font-weight: bold; text-align: center; }
                .summary-label { font-size: 9px; margin-bottom: 4px; color: #555; }
                .summary-value { font-size: 12px; }
                
                .promoted-box { margin-top: 15px; font-weight: bold; font-size: 11px; }
                .promoted-underline { border-bottom: 1px solid #333; min-width: 200px; display: inline-block; padding: 0 10px; }
                
                .footer-table { border: none !important; margin-top: 40px; }
                .footer-table td { border: none !important; text-align: center; vertical-align: bottom; }
            </style>
            </head>
            <body>
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
                            <th rowspan="3" class="section-header" style="width: 140px;">SUBJECTS</th>
                            <th colspan="6" class="section-header">TERM - 1 (100)</th>
                            <th colspan="6" class="section-header">TERM - 2 (100)</th>
                            <th colspan="2" class="section-header">OVERALL</th>
                        </tr>
                        <tr class="sub-header">
                            <th rowspan="2">PT<br/>(10)</th><th rowspan="2">NB<br/>(5)</th><th rowspan="2">SE<br/>(5)</th><th rowspan="2">TERM 1<br/>(80)</th><th rowspan="2" style="background-color:#e5e7eb">TOTAL<br/>(100)</th><th rowspan="2">GR</th>
                            <th rowspan="2">PT<br/>(10)</th><th rowspan="2">NB<br/>(5)</th><th rowspan="2">SE<br/>(5)</th><th rowspan="2">TERM 2<br/>(80)</th><th rowspan="2" style="background-color:#e5e7eb">TOTAL<br/>(100)</th><th rowspan="2">GR</th>
                            <th style="font-size: 7px;">T1(50%)+T2(50%)</th><th rowspan="2">GR</th>
                        </tr>
                        <tr class="sub-header">
                            <th>GRAD TOTAL</th>
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
                                <div class="summary-label">OVERALL GRADE</div>
                                <div class="summary-value">${GRADE}</div>
                            </div>
                        </td>
                    </tr>
                </table>
                
                <table style="margin-top: 15px;">
                    <thead>
                        <tr>
                            <th colspan="4" class="section-header">CO-SCHOLASTIC AREA</th>
                        </tr>
                        <tr class="sub-header" style="font-weight: bold;">
                            <th style="width: 35%;">TERM-1 (A-C scale)</th>
                            <th style="width: 15%;">GRADE</th>
                            <th style="width: 35%;">TERM-2 (A-C scale)</th>
                            <th style="width: 15%;">GRADE</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${ACTIVITY_ROWS}
                    </tbody>
                </table>
                
                <div class="promoted-box">
                    RESULT: PROMOTED TO CLASS : <span class="promoted-underline">${PROMOTED_TO}</span>
                </div>
                
                <table class="footer-table">
                    <tr>
                        <td width="25%">Date : ${DATE}</td>
                        <td width="25%">School Stamp</td>
                        <td width="25%">Class Teacher</td>
                        <td width="25%">Principal</td>
                    </tr>
                </table>
            </div>
            </body>
            </html>
            """;
}