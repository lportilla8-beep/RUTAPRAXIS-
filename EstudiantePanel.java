import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class EstudiantePanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel inicioPanel;

    private JPanel[] clasePanels = new JPanel[6];

    // Lista compartida con el Tutor
    public static ArrayList<TareaEntregada> tareasEntregadas = new ArrayList<>();
    public static ArrayList<TareaAsignada> tareasAsignadas = new ArrayList<>();

    public EstudiantePanel() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(139, 115, 85));
        JLabel headerLabel = new JLabel("RUTA PRAXIS - ESTUDIANTE", SwingConstants.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        top.add(headerLabel, BorderLayout.CENTER);

        JPanel sidebar = createSidebar();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        inicioPanel = new JPanel(new GridLayout(2, 3, 25, 25));
        inicioPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        inicioPanel.setBackground(new Color(245, 245, 240));

        contentPanel.add(inicioPanel, "INICIO");
        contentPanel.add(createPendientesPanel(), "PENDIENTES");

        for (int i = 1; i <= 5; i++) {
            clasePanels[i] = new JPanel(new BorderLayout(15, 15));
            clasePanels[i].setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
            clasePanels[i].setBackground(new Color(245, 245, 240));
            contentPanel.add(clasePanels[i], "CLASE_" + i);
        }

        add(top, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                actualizarInicioPanel();
            }
        });

        actualizarInicioPanel();
        cardLayout.show(contentPanel, "INICIO");
    }

    private void mostrarClase(int numClase) {
        actualizarClasePanel(numClase);
        cardLayout.show(contentPanel, "CLASE_" + numClase);
    }

    private TareaEntregada obtenerTarea(int numClase) {
        for (TareaEntregada t : tareasEntregadas) {
            if (t.getClase().equalsIgnoreCase("Clase " + numClase)) {
                return t;
            }
        }
        return null;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(139, 115, 85));
        sidebar.setPreferredSize(new Dimension(250, 0));

        addSidebarButton(sidebar, "Inicio", e -> {
            actualizarInicioPanel();
            cardLayout.show(contentPanel, "INICIO");
        });
        addSidebarButton(sidebar, "Calendario", null);

        JLabel clasesLabel = new JLabel("   CLASES ▼");
        clasesLabel.setForeground(Color.WHITE);
        clasesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        clasesLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 8, 0));
        sidebar.add(clasesLabel);

        for (int i = 1; i <= 5; i++) {
            final int num = i;
            addSidebarButton(sidebar, "   CLASE " + i, e -> mostrarClase(num));
        }

        addSidebarButton(sidebar, "Pendientes", e -> cardLayout.show(contentPanel, "PENDIENTES"));

        sidebar.add(Box.createVerticalGlue());
        addSidebarButton(sidebar, "Cerrar Sesión", e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof RutaPraxisApp) {
                ((RutaPraxisApp) frame).volverAlLogin();
            }
        });

        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, 48));
        btn.setBackground(new Color(160, 130, 90));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        if (listener != null) btn.addActionListener(listener);
        sidebar.add(btn);
    }

    private void actualizarInicioPanel() {
        inicioPanel.removeAll();
        for (int i = 1; i <= 5; i++) {
            inicioPanel.add(createClassCard("CLASES " + (char)('I' + i - 1), i));
        }
        inicioPanel.revalidate();
        inicioPanel.repaint();
    }

    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private JPanel createClassCard(String nombre, int num) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(210, 205, 195), 2));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(175, 150, 115), getWidth(), 0, new Color(139, 115, 85));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 50));
        header.setLayout(new BorderLayout());
        JLabel headerLbl = new JLabel(nombre, SwingConstants.CENTER);
        headerLbl.setFont(new Font("Arial", Font.BOLD, 16));
        headerLbl.setForeground(Color.WHITE);
        header.add(headerLbl, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        TareaAsignada ta = obtenerTareaAsignada(num);
        if (ta != null) {
            JLabel lblTaskTitle = new JLabel("<html><div style='text-align: center; width: 180px;'><b>Tarea:</b> " + ta.titulo + "</div></html>");
            lblTaskTitle.setFont(new Font("Arial", Font.PLAIN, 12));
            lblTaskTitle.setForeground(new Color(60, 50, 40));
            lblTaskTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblTaskDate = new JLabel("<html>📅 <b>Límite:</b> " + ta.fechaLimite + "</html>");
            lblTaskDate.setFont(new Font("Arial", Font.PLAIN, 11));
            lblTaskDate.setForeground(new Color(80, 80, 80));
            lblTaskDate.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Determinar estado de la entrega/tarea
            TareaEntregada te = obtenerTarea(num);
            StatusBadge badge;
            String btnLabel = "Ver Tarea ➔";
            Color btnNormal = new Color(139, 115, 85);
            Color btnHover = new Color(165, 140, 110);

            if (te != null) {
                if (te.estado.equalsIgnoreCase("Calificada")) {
                    badge = new StatusBadge("CALIFICADA (" + te.calificacion + ")", new Color(219, 234, 254), new Color(29, 78, 216));
                    btnLabel = "Ver Nota ➔";
                    btnNormal = new Color(29, 78, 216);
                    btnHover = new Color(59, 130, 246);
                } else {
                    badge = new StatusBadge("ENTREGADA", new Color(220, 252, 231), new Color(21, 128, 61));
                    btnLabel = "Ver Entrega ➔";
                    btnNormal = new Color(21, 128, 61);
                    btnHover = new Color(34, 197, 94);
                }
            } else {
                if (determinarEstadoAsignacion(ta).startsWith("Vencida")) {
                    badge = new StatusBadge("VENCIDA", new Color(254, 226, 226), new Color(185, 28, 28));
                    btnLabel = "Ver Detalles ➔";
                    btnNormal = new Color(185, 28, 28);
                    btnHover = new Color(239, 68, 68);
                } else {
                    badge = new StatusBadge("PENDIENTE", new Color(254, 243, 199), new Color(180, 83, 9));
                    btnLabel = "Entregar Tarea ➔";
                    btnNormal = new Color(180, 83, 9);
                    btnHover = new Color(245, 158, 11);
                }
            }

            body.add(Box.createVerticalGlue());
            body.add(lblTaskTitle);
            body.add(Box.createRigidArea(new Dimension(0, 8)));
            body.add(lblTaskDate);
            body.add(Box.createRigidArea(new Dimension(0, 10)));
            
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(badge);
            body.add(badgePanel);
            
            body.add(Box.createRigidArea(new Dimension(0, 12)));
            
            ModernButton actionBtn = new ModernButton(btnLabel, btnNormal, btnHover);
            actionBtn.setPreferredSize(new Dimension(160, 32));
            actionBtn.setMaximumSize(new Dimension(160, 32));
            actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionBtn.addActionListener(e -> mostrarClase(num));
            body.add(actionBtn);
            
            body.add(Box.createVerticalGlue());
        } else {
            JLabel noTaskLbl = new JLabel("Sin tareas asignadas", SwingConstants.CENTER);
            noTaskLbl.setFont(new Font("Arial", Font.ITALIC, 13));
            noTaskLbl.setForeground(Color.GRAY);
            noTaskLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            StatusBadge badge = new StatusBadge("SIN TAREAS", new Color(243, 244, 246), new Color(107, 114, 128));

            body.add(Box.createVerticalGlue());
            body.add(noTaskLbl);
            body.add(Box.createRigidArea(new Dimension(0, 10)));
            
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            badgePanel.setOpaque(false);
            badgePanel.add(badge);
            body.add(badgePanel);
            
            body.add(Box.createRigidArea(new Dimension(0, 15)));
            
            ModernButton actionBtn = new ModernButton("Ir a la Clase ➔", new Color(156, 163, 175), new Color(107, 114, 128));
            actionBtn.setPreferredSize(new Dimension(160, 32));
            actionBtn.setMaximumSize(new Dimension(160, 32));
            actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionBtn.addActionListener(e -> mostrarClase(num));
            body.add(actionBtn);
            
            body.add(Box.createVerticalGlue());
        }

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(new Color(139, 115, 85), 2));
                card.setBackground(new Color(250, 248, 243));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                Point p = e.getPoint();
                if (p.x < 0 || p.x >= card.getWidth() || p.y < 0 || p.y >= card.getHeight()) {
                    card.setBorder(BorderFactory.createLineBorder(new Color(210, 205, 195), 2));
                    card.setBackground(Color.WHITE);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarClase(num);
            }
        });
        return card;
    }

    private TareaAsignada obtenerTareaAsignada(int numClase) {
        for (TareaAsignada ta : tareasAsignadas) {
            if (ta.clase.equalsIgnoreCase("Clase " + numClase)) {
                return ta;
            }
        }
        return null;
    }

    private String determinarEstadoAsignacion(TareaAsignada ta) {
        if (ta == null) return "";
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            java.time.LocalDateTime limite = java.time.LocalDateTime.parse(ta.fechaLimite + " " + ta.horaLimite, formatter);
            if (java.time.LocalDateTime.now().isAfter(limite)) {
                return "Vencida (Fecha límite superada)";
            } else {
                return "Vigente";
            }
        } catch (Exception e) {
            return "Vigente";
        }
    }

    private String determinarEstadoEntrega(TareaEntregada te, TareaAsignada ta) {
        if (te == null || ta == null) return "Pendiente";
        if (te.estado.equalsIgnoreCase("Calificada")) {
            return "Calificada";
        }
        try {
            java.time.format.DateTimeFormatter formatterLim = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            java.time.format.DateTimeFormatter formatterEnt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            java.time.LocalDateTime limite = java.time.LocalDateTime.parse(ta.fechaLimite + " " + ta.horaLimite, formatterLim);
            java.time.LocalDateTime entrega = java.time.LocalDateTime.parse(te.fechaEntrega + " " + te.horaEntrega, formatterEnt);
            if (entrega.isAfter(limite)) {
                return "Entregado (Fuera de plazo)";
            } else {
                return "Entregado (A tiempo)";
            }
        } catch (Exception e) {
            return "Entregado";
        }
    }

    private void actualizarClasePanel(int numClase) {
        JPanel panel = clasePanels[numClase];
        panel.removeAll();

        JLabel titulo = new JLabel("CLASE " + numClase + " - Panel de Tarea", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(new Color(80, 60, 40));
        panel.add(titulo, BorderLayout.NORTH);

        TareaAsignada tareaAsignada = obtenerTareaAsignada(numClase);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(245, 245, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        if (tareaAsignada == null) {
            JPanel noAsignadoPanel = new JPanel(new BorderLayout());
            noAsignadoPanel.setBackground(Color.WHITE);
            noAsignadoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));

            JLabel lblInfo = new JLabel("El tutor académico no ha asignado ninguna tarea para esta clase todavía.", SwingConstants.CENTER);
            lblInfo.setFont(new Font("Arial", Font.ITALIC, 15));
            lblInfo.setForeground(Color.GRAY);
            noAsignadoPanel.add(lblInfo, BorderLayout.CENTER);

            content.add(noAsignadoPanel, gbc);
        } else {
            JPanel taskInfoPanel = new JPanel(new GridBagLayout());
            taskInfoPanel.setBackground(Color.WHITE);
            taskInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(139, 115, 85), 2), "TAREA ASIGNADA POR EL TUTOR"),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            GridBagConstraints tgbc = new GridBagConstraints();
            tgbc.insets = new Insets(5, 5, 5, 5);
            tgbc.anchor = GridBagConstraints.WEST;
            tgbc.fill = GridBagConstraints.HORIZONTAL;
            tgbc.weightx = 1.0;

            tgbc.gridx = 0; tgbc.gridy = 0;
            JLabel lblTitulo = new JLabel("📌 Título: " + tareaAsignada.titulo);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 15));
            taskInfoPanel.add(lblTitulo, tgbc);

            tgbc.gridy = 1;
            JLabel lblDesc = new JLabel("<html>📝 <b>Descripción:</b> " + tareaAsignada.descripcion.replace("\n", "<br>") + "</html>");
            lblDesc.setFont(new Font("Arial", Font.PLAIN, 13));
            taskInfoPanel.add(lblDesc, tgbc);

            tgbc.gridy = 2;
            JPanel fileRowTutor = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
            fileRowTutor.setBackground(Color.WHITE);
            JLabel lblArchivoTutor = new JLabel("📄 Archivo de la Tarea: " + 
                (tareaAsignada.archivoTutor != null && !tareaAsignada.archivoTutor.isEmpty() ? new java.io.File(tareaAsignada.archivoTutor).getName() : "Sin archivo adjunto"));
            lblArchivoTutor.setFont(new Font("Arial", Font.PLAIN, 13));
            fileRowTutor.add(lblArchivoTutor);
            if (tareaAsignada.archivoTutor != null && !tareaAsignada.archivoTutor.isEmpty()) {
                JButton btnOpenTutorFile = new JButton("Abrir");
                btnOpenTutorFile.setFont(new Font("Arial", Font.BOLD, 11));
                btnOpenTutorFile.setBackground(new Color(139, 115, 85));
                btnOpenTutorFile.setForeground(Color.WHITE);
                btnOpenTutorFile.setFocusPainted(false);
                btnOpenTutorFile.addActionListener(ev -> abrirArchivo(tareaAsignada.archivoTutor));
                fileRowTutor.add(btnOpenTutorFile);
            }
            taskInfoPanel.add(fileRowTutor, tgbc);

            tgbc.gridy = 3;
            String estAsig = determinarEstadoAsignacion(tareaAsignada);
            JLabel lblPlazo = new JLabel("📅 Fecha Límite: " + tareaAsignada.fechaLimite + " a las " + tareaAsignada.horaLimite + 
                " | Estado: " + estAsig);
            lblPlazo.setFont(new Font("Arial", Font.BOLD, 13));
            if (estAsig.startsWith("Vencida")) {
                lblPlazo.setForeground(Color.RED);
            } else {
                lblPlazo.setForeground(new Color(0, 120, 0));
            }
            taskInfoPanel.add(lblPlazo, tgbc);

            content.add(taskInfoPanel, gbc);

            gbc.gridy = 1;
            TareaEntregada entrega = obtenerTarea(numClase);

            if (entrega != null) {
                JPanel entregaPanel = new JPanel(new GridLayout(6, 1, 5, 5));
                entregaPanel.setBackground(Color.WHITE);
                entregaPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(194, 170, 120), 2), "TU ENTREGA"),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));

                JPanel fileRowStudent = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
                fileRowStudent.setBackground(Color.WHITE);
                JLabel lblArchivo = new JLabel("📄 Tu Archivo: " + new java.io.File(entrega.archivo).getName());
                lblArchivo.setFont(new Font("Arial", Font.PLAIN, 13));
                fileRowStudent.add(lblArchivo);
                JButton btnOpenStudentFile = new JButton("Abrir");
                btnOpenStudentFile.setFont(new Font("Arial", Font.BOLD, 11));
                btnOpenStudentFile.setBackground(new Color(139, 115, 85));
                btnOpenStudentFile.setForeground(Color.WHITE);
                btnOpenStudentFile.setFocusPainted(false);
                btnOpenStudentFile.addActionListener(ev -> abrirArchivo(entrega.archivo));
                fileRowStudent.add(btnOpenStudentFile);
                
                JLabel lblFecha = new JLabel("📅 Entregado el: " + entrega.fechaEntrega);
                lblFecha.setFont(new Font("Arial", Font.PLAIN, 13));
                
                JLabel lblHora = new JLabel("⏰ Hora de Entrega: " + entrega.horaEntrega);
                lblHora.setFont(new Font("Arial", Font.PLAIN, 13));

                String estEnt = determinarEstadoEntrega(entrega, tareaAsignada);
                JLabel lblEstado = new JLabel("ℹ️ Estado de Entrega: " + estEnt);
                lblEstado.setFont(new Font("Arial", Font.BOLD, 13));
                if (estEnt.contains("Fuera de plazo")) {
                    lblEstado.setForeground(Color.RED);
                } else if (estEnt.contains("A tiempo")) {
                    lblEstado.setForeground(new Color(0, 120, 0));
                } else if (entrega.estado.equalsIgnoreCase("Calificada")) {
                    lblEstado.setForeground(new Color(0, 100, 200));
                } else {
                    lblEstado.setForeground(new Color(200, 120, 0));
                }

                JLabel lblCalificacion = new JLabel("⭐ Calificación: " + (entrega.calificacion > 0 ? entrega.calificacion : "Sin calificar"));
                lblCalificacion.setFont(new Font("Arial", Font.PLAIN, 13));

                JLabel lblComentario = new JLabel("💬 Comentarios del Tutor: " + 
                    (entrega.comentario != null && !entrega.comentario.isEmpty() ? entrega.comentario : "Sin comentarios aún"));
                lblComentario.setFont(new Font("Arial", Font.ITALIC, 13));
                lblComentario.setForeground(Color.DARK_GRAY);

                entregaPanel.add(fileRowStudent);
                entregaPanel.add(lblFecha);
                entregaPanel.add(lblHora);
                entregaPanel.add(lblEstado);
                entregaPanel.add(lblCalificacion);
                entregaPanel.add(lblComentario);

                content.add(entregaPanel, gbc);

                gbc.gridy = 2;
                JButton btnReupload = new JButton("Volver a subir tarea (Reemplazar Entrega)");
                btnReupload.setBackground(new Color(139, 115, 85));
                btnReupload.setForeground(Color.WHITE);
                btnReupload.setFocusPainted(false);
                btnReupload.setFont(new Font("Arial", Font.BOLD, 14));
                btnReupload.setPreferredSize(new Dimension(280, 40));
                btnReupload.addActionListener(e -> subirTarea(numClase));
                content.add(btnReupload, gbc);
            } else {
                JPanel noEntregaPanel = new JPanel(new BorderLayout(10, 10));
                noEntregaPanel.setBackground(Color.WHITE);
                noEntregaPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2), "TU ENTREGA"),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));

                JLabel lblInfo = new JLabel("No has entregado ninguna solución para esta tarea.", SwingConstants.CENTER);
                lblInfo.setFont(new Font("Arial", Font.BOLD, 13));
                lblInfo.setForeground(Color.GRAY);
                noEntregaPanel.add(lblInfo, BorderLayout.CENTER);

                JButton btnUpload = new JButton("Subir Archivo de Entrega");
                btnUpload.setBackground(new Color(139, 115, 85));
                btnUpload.setForeground(Color.WHITE);
                btnUpload.setFocusPainted(false);
                btnUpload.setFont(new Font("Arial", Font.BOLD, 15));
                btnUpload.setPreferredSize(new Dimension(180, 40));
                btnUpload.addActionListener(e -> subirTarea(numClase));
                noEntregaPanel.add(btnUpload, BorderLayout.SOUTH);

                content.add(noEntregaPanel, gbc);
            }
        }

        panel.add(content, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void subirTarea(int numClase) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatFecha = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter formatHora = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
            
            String fecha = ahora.format(formatFecha);
            String hora = ahora.format(formatHora);

            TareaAsignada ta = obtenerTareaAsignada(numClase);
            String tituloTarea = (ta != null) ? ta.titulo : "Tarea " + numClase;

            TareaEntregada t = obtenerTarea(numClase);
            if (t != null) {
                t.archivo = f.getAbsolutePath();
                t.fechaEntrega = fecha;
                t.horaEntrega = hora;
                t.estado = "Pendiente";
                t.comentario = "";
                t.calificacion = 0;
            } else {
                t = new TareaEntregada("Estudiante", "Clase " + numClase, tituloTarea, f.getAbsolutePath(), fecha, hora);
                tareasEntregadas.add(t);
            }
            
            JOptionPane.showMessageDialog(this, "Tarea enviada al Tutor con éxito.");
            actualizarClasePanel(numClase);
        }
    }

    private JPanel createPendientesPanel() {
        JPanel p = new JPanel();
        p.add(new JLabel("Tareas Pendientes - Listo para Base de Datos"));
        return p;
    }

    private void abrirArchivo(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ningún archivo asociado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.io.File archivo = new java.io.File(ruta);
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo no existe en la ruta especificada:\n" + ruta, "Archivo no encontrado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(this, "La apertura de archivos no está soportada en este sistema.", "Soporte no disponible", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo: " + ex.getMessage(), "Error al abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====================== COMPONENTES VISUALES PREMIUM ======================
    private static class StatusBadge extends JPanel {
        private Color bgColor;
        private Color textColor;

        public StatusBadge(String text, Color bgColor, Color textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
            setOpaque(false);
            setLayout(new BorderLayout());
            
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 10));
            label.setForeground(textColor);
            label.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;

        public ModernButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverColor);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normalColor);
                    repaint();
                }
            });
            setBackground(normalColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}