from reportlab.lib import colors
from reportlab.lib.pagesizes import letter, landscape
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle
from reportlab.pdfgen import canvas

OUT = "/workspace/scratch/2176a3fdf162/docs"

def draw_box(c, x, y, w, h, title, lines, fill=colors.whitesmoke):
    c.setFillColor(fill); c.setStrokeColor(colors.HexColor('#24527a'))
    c.roundRect(x, y, w, h, 8, fill=1, stroke=1)
    c.setFillColor(colors.HexColor('#17324d')); c.setFont('Helvetica-Bold', 11)
    c.drawString(x + 8, y + h - 18, title)
    c.setFillColor(colors.black); c.setFont('Helvetica', 8)
    for i, line in enumerate(lines):
        c.drawString(x + 8, y + h - 32 - i * 12, line)

def arrow(c, x1, y1, x2, y2):
    c.setStrokeColor(colors.HexColor('#555555')); c.line(x1, y1, x2, y2)
    c.setFillColor(colors.HexColor('#555555'))
    c.line(x2, y2, x2 - 6, y2 + 3); c.line(x2, y2, x2 - 6, y2 - 3)

def create_er():
    path = f"{OUT}/er-diagram.pdf"
    c = canvas.Canvas(path, pagesize=landscape(letter)); w, h = landscape(letter)
    c.setTitle('CareConnect ER Diagram')
    c.setFont('Helvetica-Bold', 18); c.drawString(36, h - 40, 'CareConnect ER Diagram')
    draw_box(c, 35, 330, 145, 120, 'USERS', ['PK user_id', 'name', 'email UNIQUE', 'password_hash', 'role'])
    draw_box(c, 225, 330, 155, 120, 'PROVIDERS', ['PK provider_id', 'FK user_id UNIQUE', 'specialization', 'bio', 'license_number'], colors.HexColor('#eef6fb'))
    draw_box(c, 430, 330, 155, 120, 'SERVICES', ['PK service_id', 'service_name UNIQUE', 'description', 'duration_minutes', 'price'], colors.HexColor('#eef6fb'))
    draw_box(c, 160, 100, 190, 140, 'AVAILABILITY_SLOTS', ['PK slot_id', 'FK provider_id', 'FK service_id', 'start_time', 'end_time', 'status'])
    draw_box(c, 405, 100, 190, 140, 'APPOINTMENTS', ['PK appointment_id', 'FK slot_id UNIQUE', 'FK customer_id', 'status', 'booked_at'], colors.HexColor('#fff4e6'))
    arrow(c, 180, 390, 225, 390); arrow(c, 300, 330, 255, 240); arrow(c, 505, 330, 305, 240); arrow(c, 350, 170, 405, 170); arrow(c, 105, 330, 450, 240)
    c.setFont('Helvetica', 8); c.drawString(185, 398, '1 : 0..1'); c.drawString(277, 285, '1 : many'); c.drawString(413, 285, '1 : many'); c.drawString(355, 180, '1 : 0..1'); c.drawString(245, 265, 'patient books')
    c.setFont('Helvetica-Oblique', 9); c.drawString(36, 45, 'The UNIQUE(slot_id) constraint ensures that one availability slot cannot be booked twice.')
    c.save()

def create_architecture():
    path = f"{OUT}/architecture-diagram.pdf"
    c = canvas.Canvas(path, pagesize=landscape(letter)); w, h = landscape(letter)
    c.setFont('Helvetica-Bold', 18); c.drawString(36, h - 40, 'CareConnect Architecture')
    draw_box(c, 40, 275, 150, 75, 'Client', ['Browser or REST client'], colors.HexColor('#eef6fb'))
    draw_box(c, 245, 250, 190, 125, 'Spring Boot Application', ['Controller', 'Service', 'Repository / JDBC', 'DTO responses'], colors.HexColor('#e9f7ef'))
    draw_box(c, 500, 275, 120, 75, 'SQL Database', ['H2', 'schema.sql', 'seed.sql'], colors.HexColor('#fff4e6'))
    draw_box(c, 285, 105, 165, 70, 'Mock External Service', ['Future email/SMS', 'Milestone 3'], colors.HexColor('#f5eefb'))
    arrow(c, 190, 312, 245, 312); arrow(c, 435, 312, 500, 312); arrow(c, 340, 250, 365, 175)
    c.setFont('Helvetica', 9); c.drawString(36, 55, 'Request flow: Client → Controller → Service → Repository/JDBC → Database')
    c.save()

def create_wireframes():
    path = f"{OUT}/wireframes.pdf"
    c = canvas.Canvas(path, pagesize=letter); w, h = letter
    pages = [
        ('1. Home', ['CareConnect', 'Clinic appointment booking made easier', '', '[ Browse Available Slots ]', '[ Patient / Provider Login ]', '', 'Services: General Consultation | Physical Therapy | Preventive Health']),
        ('2. View Available Slots', ['Available Clinic Appointments', '', 'Provider: [ Any ]   Service: [ Any ]   Date: [ Select date ]', '', 'Provider             Service                 Time             Action', 'Dr. Maya Chen       General Consultation    9:00–9:30 AM     [Book]', 'Jordan Rivera       Physical Therapy        2:00–3:00 PM     [Book]', '', '[Previous]                                              [Next]']),
        ('3. Book Appointment', ['Book Your Appointment', '', 'Provider: Dr. Maya Chen', 'Service: General Consultation', 'Date: October 5, 2026', 'Time: 9:00 AM – 9:30 AM', '', 'Patient name: [________________________]', 'Email:        [________________________]', '', '[ Confirm Booking ]       [ Go Back ]']),
        ('4. Confirmation', ['Appointment Confirmed', '', 'Your appointment has been booked successfully.', '', 'Appointment ID: 1001', 'Provider: Dr. Maya Chen', 'Service: General Consultation', 'Date: October 5, 2026', 'Time: 9:00 AM – 9:30 AM', '', 'A confirmation notification will be sent.', '', '[ View My Appointments ]       [ Return Home ]'])]
    for title, lines in pages:
        c.setFont('Helvetica-Bold', 18); c.drawString(50, h - 55, title)
        c.setStrokeColor(colors.HexColor('#24527a')); c.setLineWidth(2); c.rect(50, 120, w - 100, h - 220)
        c.setFont('Helvetica', 12); y = h - 110
        for line in lines:
            c.drawString(75, y, line); y -= 28
        c.showPage()
    c.save()

def create_report():
    styles = getSampleStyleSheet(); styles.add(ParagraphStyle(name='Small', parent=styles['BodyText'], fontSize=9, leading=12))
    story = []
    with open(f'{OUT}/milestone1-report.md') as f: text = f.read()
    for block in text.split('\n\n'):
        if not block.strip(): continue
        lines = block.splitlines(); first = lines[0]
        if first.startswith('# '): story.append(Paragraph(first[2:], styles['Title']))
        elif first.startswith('## '): story.append(Paragraph(first[3:], styles['Heading1']))
        elif first.startswith('### '): story.append(Paragraph(first[4:], styles['Heading2']))
        elif first.startswith('```'):
            story.append(Paragraph('<br/>'.join(lines[1:-1]).replace(' ', '&nbsp;'), styles['Small']))
        else:
            cleaned = '<br/>'.join(line.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;') for line in lines)
            story.append(Paragraph(cleaned, styles['BodyText']))
        story.append(Spacer(1, 8))
    doc = SimpleDocTemplate(f'{OUT}/milestone1-report.pdf', pagesize=letter, rightMargin=45, leftMargin=45, topMargin=45, bottomMargin=45)
    doc.build(story)

create_er(); create_architecture(); create_wireframes(); create_report()
