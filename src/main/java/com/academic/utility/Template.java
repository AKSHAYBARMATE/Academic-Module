package com.academic.utility;

public class Template {

    public static final String TERM_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
            
            body { font-family: Arial; font-size: 12px; }
            
            table { width:100%; border-collapse:collapse; }
            
            th,td { border:1px solid black; padding:5px; text-align:center; }
            
            .header td { border:none; }
            
            .school-name {
                font-size:22px;
                font-weight:bold;
                color:#0b3d91;
                text-align:center;
            }
            
            .logo { height:60px; }
            
            .section { background:#eaeaea; font-weight:bold; }
            
            .no-border td { border:none; }
            
            .footer { margin-top:20px; }
            
            .footer td { border:none; text-align:center; }
            
            </style>
            </head>
            
            <body>
            
            <table class="header">
            <tr>
            <td width="20%"><img src="${LEFT_LOGO}" class="logo"/></td>
            <td width="60%" class="school-name">${SCHOOL_NAME}</td>
            <td width="20%" align="right"><img src="${RIGHT_LOGO}" class="logo"/></td>
            </tr>
            </table>
            
            <div style="text-align:center; font-weight:bold; margin-top:5px;">
            ${REPORT_TITLE}
            </div>
            
            <div style="text-align:center;">Session: ${SESSION}</div>
            
            <!-- STUDENT DETAILS -->
            <table style="width:100%; margin-top:10px;" class="no-border">
            <tr>
            
            <td width="50%">
            
            <table class="no-border">
            <tr><td>Student Name :</td><td><b>${STUDENT_NAME}</b></td></tr>
            <tr><td>Father's Name :</td><td>${FATHER_NAME}</td></tr>
            <tr><td>Mother's Name :</td><td>${MOTHER_NAME}</td></tr>
            </table>
            
            </td>
            
            <td width="50%">
            
            <table class="no-border">
            <tr><td>Class :</td><td>${CLASS}</td></tr>
            <tr><td>Section :</td><td>${SECTION}</td></tr>
            <tr><td>Roll No :</td><td>${ROLL_NO}</td></tr>
            <tr><td>DOB :</td><td>${DOB}</td></tr>
            </table>
            
            </td>
            
            </tr>
            </table>
            
            <br/>
            
            <table>
            <tr><th colspan="6" class="section">SCHOLASTIC AREA</th></tr>
            
            <tr>
            <th>SUBJECT</th>
            <th>PT</th>
            <th>NB</th>
            <th>SE</th>
            <th>TERM</th>
            <th>GRADE</th>
            </tr>
            
            ${SUBJECT_ROWS}
            
            </table>
            
            <br/>
            
            <table>
            <tr>
            
            <td>
            <b>OVERALL MARKS</b><br/>
            ${TOTAL_MARKS} / ${TOTAL_MAX}
            </td>
            
            <td>
            <b>PERCENTAGE</b><br/>
            ${PERCENTAGE}
            </td>
            
            <td>
            <b>GRADE</b><br/>
            ${GRADE}
            </td>
            
            </tr>
            </table>
            
            <br/>
            
            <table>
            <tr><th colspan="2" class="section">CO-SCHOLASTIC</th></tr>
            <tr><th>Activity</th><th>Grade</th></tr>
            
            ${ACTIVITY_ROWS}
            
            </table>
            
            <table class="footer">
            <tr>
            <td>Date: ${DATE}</td>
            <td>Class Teacher</td>
            <td>Principal</td>
            </tr>
            </table>
            
            </body>
            </html>
            """;


    public static final String ANNUAL_MARKSHEET_HTML = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
            
            body { font-family: Arial; font-size: 12px; }
            
            table { width:100%; border-collapse:collapse; }
            
            th,td { border:1px solid black; padding:5px; text-align:center; }
            
            .header td { border:none; }
            
            .school-name {
                font-size:22px;
                font-weight:bold;
                color:#0b3d91;
                text-align:center;
            }
            
            .logo { height:60px; }
            
            .section { background:#eaeaea; font-weight:bold; }
            
            .no-border td { border:none; }
            
            .footer { margin-top:20px; }
            
            .footer td { border:none; text-align:center; }
            
            </style>
            </head>
            
            <body>
            
            <table class="header">
            <tr>
            <td width="20%"><img src="${LEFT_LOGO}" class="logo"/></td>
            <td width="60%" class="school-name">${SCHOOL_NAME}</td>
            <td width="20%" align="right"><img src="${RIGHT_LOGO}" class="logo"/></td>
            </tr>
            </table>
            
            <div style="text-align:center; font-weight:bold;">ANNUAL PROGRESS REPORT</div>
            <div style="text-align:center;">Session: ${SESSION}</div>
            
            <!-- STUDENT -->
            <table style="width:100%; margin-top:10px;" class="no-border">
            <tr>
            
            <td width="50%">
            <table class="no-border">
            <tr><td>Student Name :</td><td><b>${STUDENT_NAME}</b></td></tr>
            <tr><td>Father's Name :</td><td>${FATHER_NAME}</td></tr>
            <tr><td>Mother's Name :</td><td>${MOTHER_NAME}</td></tr>
            </table>
            </td>
            
            <td width="50%">
            <table class="no-border">
            <tr><td>Class :</td><td>${CLASS}</td></tr>
            <tr><td>Section :</td><td>${SECTION}</td></tr>
            <tr><td>Roll No :</td><td>${ROLL_NO}</td></tr>
            <tr><td>DOB :</td><td>${DOB}</td></tr>
            </table>
            </td>
            
            </tr>
            </table>
            
            <br/>
            
            <table>
            
            <tr>
            <th rowspan="2">SUBJECT</th>
            <th colspan="5">TERM-1</th>
            <th colspan="5">TERM-2</th>
            <th colspan="2">OVERALL</th>
            </tr>
            
            <tr>
            <th>PT</th><th>NB</th><th>SE</th><th>TERM</th><th>GRADE</th>
            <th>PT</th><th>NB</th><th>SE</th><th>TERM</th><th>GRADE</th>
            <th>TOTAL</th><th>GRADE</th>
            </tr>
            
            ${SUBJECT_ROWS}
            
            </table>
            
            <br/>
            
            <table>
            <tr>
            
            <td>
            <b>OVERALL MARKS</b><br/>
            ${TOTAL_MARKS} / ${TOTAL_MAX}
            </td>
            
            <td>
            <b>PERCENTAGE</b><br/>
            ${PERCENTAGE}
            </td>
            
            <td>
            <b>OVERALL GRADE</b><br/>
            ${GRADE}
            </td>
            
            </tr>
            </table>
            
            <br/>
            
            <table>
            <tr><th colspan="4" class="section">CO-SCHOLASTIC</th></tr>
            <tr>
            <th>Activity</th><th>Grade</th>
            <th>Activity</th><th>Grade</th>
            </tr>
            
            ${ACTIVITY_ROWS}
            
            </table>
            
            <table class="footer">
            <tr>
            <td>Date: ${DATE}</td>
            <td>Class Teacher</td>
            <td>Principal</td>
            </tr>
            </table>
            
            </body>
            </html>
            """;
}