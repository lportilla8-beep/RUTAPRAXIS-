import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AsesorPanel extends JPanel {
    private final Color mainColor = new Color(12, 43, 102);
    private final Color buttonColor = new Color(30, 64, 175);
    private final Color hoverColor = new Color(37, 99, 235);
    private final Color bgColor = new Color(239, 246, 255);
    private final Color borderColor = new Color(191, 219, 254);

    private CardLayout asesorCardLayout;
    private JPanel asesorContentPanel;
    private JPanel bitacorasPanel;
    private JPanel[] clasePanels = new JPanel[6];

    private static final Map<String, String> observacionesSinEntrega = new HashMap<>();

    public AsesorPanel() {
        setLayout(new BorderLayout());
        add(new ModernTopBar("RUTA PRAXIS - ASESOR PEDAGOGICO"), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        asesorCardLayout = new CardLayout();
        asesorContentPanel = new JPanel(asesorCardLayout);

        bitacorasPanel = createBitacorasPanel();
        asesorContentPanel.add(bitacorasPanel, "BITACORAS");

        for (int i = 1; i <= 5; i++) {
            clasePanels[i] = new JPanel(new BorderLayout(16, 16));
            clasePanels[i].setBackground(bgColor);
            clasePanels[i].setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
            asesorContentPanel.add(clasePanels[i], "CLASE_" + i);
        }

        add(asesorContentPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                mostrarBitacoras();
            }
        });
        asesorCardLayout.show(asesorContentPanel, "BITACORAS");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(mainColor);
        sidebar.setPreferredSize(new Dimension(250, 0));
        addSidebarButton(sidebar, "Bitacoras", e -> mostrarBitacoras());
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, 48));
        btn.setBackground(buttonColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { btn.setBackground(buttonColor); }
        });
        if (listener != null) btn.addActionListener(listener);
        sidebar.add(btn);
    }

    private JPanel createBitacorasPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        cargarBitacoras(panel);
        return panel;
    }

    private void mostrarBitacoras() {
        recargarDatosPracticas();
        bitacorasPanel.removeAll();
        cargarBitacoras(bitacorasPanel);
        bitacorasPanel.revalidate();
        bitacorasPanel.repaint();
        asesorCardLayout.show(asesorContentPanel, "BITACORAS");
    }

    private void mostrarClase(int numClase) {
        recargarDatosPracticas();
        actualizarClasePanel(numClase);
        asesorCardLayout.show(asesorContentPanel, "CLASE_" + numClase);
    }

    private void recargarDatosPracticas() {
        ConexionBD.inicializarTablas();
        if (ConexionBD.isUsandoBD()) {
            EstudiantePanel.tareasAsignadas.clear();
            EstudiantePanel.tareasAsignadas.addAll(ConexionBD.cargarTareasAsignadas());
            EstudiantePanel.tareasEntregadas.clear();
            EstudiantePanel.tareasEntregadas.addAll(ConexionBD.cargarTareasEntregadas());
        }
    }

    private void cargarBitacoras(JPanel panel) {
        JLabel title = new JLabel("Bitacoras", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(mainColor);
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 25, 25));
        grid.setOpaque(false);
        for (int i = 1; i <= 5; i++) {
            grid.add(createClassCard("CLASE " + i, i));
        }
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        grid.add(empty);
        panel.add(grid, BorderLayout.CENTER);
    }

    private JPanel createClassCard(String nombre, int num) {
        TareaAsignada tarea = buscarTareaPorClase("Clase " + num);
        TareaEntregada entrega = buscarEntregaPorClase("Clase " + num);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, hoverColor, getWidth(), 0, mainColor));
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
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        body.add(Box.createVerticalGlue());
        body.add(centerLabel(tarea != null ? tarea.titulo : "Sin actividad asignada", Font.BOLD, 13, mainColor));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(centerLabel(entrega != null ? "Entrega registrada" : "Sin entrega del estudiante", Font.PLAIN, 12, buttonColor));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(centerLabel(entrega != null && entrega.calificacion > 0 ? "Nota: " + entrega.calificacion : "Sin nota", Font.BOLD, 12, mainColor));
        body.add(Box.createVerticalGlue());

        JButton open = new JButton("Ver bitacora");
        open.setBackground(buttonColor);
        open.setForeground(Color.WHITE);
        open.setFocusPainted(false);
        open.setAlignmentX(Component.CENTER_ALIGNMENT);
        open.addActionListener(e -> mostrarClase(num));
        body.add(open);
        card.add(body, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mostrarClase(num); }
        });
        return card;
    }

    private JLabel centerLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel("<html><div style='text-align:center;width:180px;'>" + escapeHtml(text) + "</div></html>", SwingConstants.CENTER);
        label.setFont(new Font("Arial", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void actualizarClasePanel(int numClase) {
        JPanel panel = clasePanels[numClase];
        panel.removeAll();

        String clase = "Clase " + numClase;
        TareaAsignada tarea = buscarTareaPorClase(clase);
        TareaEntregada entrega = buscarEntregaPorClase(clase);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("BITACORA - " + clase, SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(mainColor);
        JButton back = new JButton("Volver");
        back.setBackground(buttonColor);
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.addActionListener(e -> mostrarBitacoras());
        header.add(title, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.45;
        content.add(createDocentePanel(tarea), gbc);

        gbc.gridy = 1;
        content.add(createEstudianteNotaPanel(entrega), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.1;
        content.add(createObservacionPanel(clase, entrega), gbc);

        panel.add(content, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private JPanel createDocentePanel(TareaAsignada tarea) {
        JPanel panel = createSectionPanel("Actividad del docente");
        panel.add(createField("Titulo", tarea != null ? tarea.titulo : "Sin actividad asignada"));
        panel.add(createField("Descripcion", tarea != null ? tarea.descripcion : "No registrada"));
        panel.add(createField("Fecha limite", tarea != null ? tarea.fechaLimite + " " + tarea.horaLimite : "No registrada"));
        panel.add(createResourceRow("Archivo del docente", tarea != null ? tarea.archivoTutor : "", "Sin archivo guia"));
        return panel;
    }

    private JPanel createEstudianteNotaPanel(TareaEntregada entrega) {
        JPanel panel = createSectionPanel("Actividad del estudiante y nota");
        panel.add(createField("Estudiante", entrega != null ? entrega.estudiante : "Sin entrega"));
        panel.add(createField("Actividad enviada", entrega != null ? entrega.titulo : "No entregada"));
        panel.add(createResourceRow("Archivo enviado", entrega != null ? entrega.archivo : "", "Sin archivo enviado"));
        panel.add(createField("Fecha de entrega", entrega != null ? entrega.fechaEntrega + " " + entrega.horaEntrega : "No entregada"));
        panel.add(createField("Estado", entrega != null ? entrega.estado : "Sin entrega"));
        panel.add(createField("Nota", entrega != null && entrega.calificacion > 0 ? String.valueOf(entrega.calificacion) : "Sin calificar"));
        panel.add(createField("Comentario del tutor", entrega != null && entrega.comentario != null && !entrega.comentario.trim().isEmpty() ? entrega.comentario : "Sin comentario del tutor"));
        return panel;
    }

    private JPanel createObservacionPanel(String clase, TareaEntregada entrega) {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor, 2), "Comentario del asesor"),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)
        ));

        JTextArea area = new JTextArea(obtenerObservacion(clase, entrega), 4, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        PlaceholderSupport.apply(area, "Escriba una observacion breve para la bitacora");

        JButton save = new JButton("Guardar comentario");
        save.setBackground(mainColor);
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setFont(new Font("Arial", Font.BOLD, 13));
        save.addActionListener(e -> guardarObservacion(clase, entrega, area));

        panel.add(area, BorderLayout.CENTER);
        panel.add(save, BorderLayout.EAST);
        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor, 2), title),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }

    private JLabel createField(String label, String value) {
        JLabel field = new JLabel("<html><b>" + escapeHtml(label) + ":</b> " + escapeHtml(value) + "</html>");
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(buttonColor);
        field.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPanel createResourceRow(String label, String recurso, String emptyText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        String text = recurso != null && !recurso.trim().isEmpty()
                ? (esEnlace(recurso) ? recurso : new File(recurso).getName())
                : emptyText;
        row.add(createField(label, text));
        if (recurso != null && !recurso.trim().isEmpty()) {
            JButton open = new JButton(esEnlace(recurso) ? "Abrir enlace" : "Abrir");
            open.setFont(new Font("Arial", Font.BOLD, 11));
            open.setBackground(buttonColor);
            open.setForeground(Color.WHITE);
            open.setFocusPainted(false);
            open.addActionListener(e -> abrirRecurso(recurso));
            row.add(open);
        }
        return row;
    }

    private String obtenerObservacion(String clase, TareaEntregada entrega) {
        if (entrega != null && entrega.comentarioAsesor != null) {
            return entrega.comentarioAsesor;
        }
        return observacionesSinEntrega.getOrDefault(clase, "");
    }

    private void guardarObservacion(String clase, TareaEntregada entrega, JTextArea area) {
        String observacion = PlaceholderSupport.getText(area).trim();
        if (entrega != null) {
            entrega.comentarioAsesor = observacion;
            ConexionBD.guardarTareaEntregada(entrega);
        } else {
            observacionesSinEntrega.put(clase, observacion);
        }
        JOptionPane.showMessageDialog(this, "Comentario guardado. El estudiante y el docente lo veran en la entrega.", "Comentario guardado", JOptionPane.INFORMATION_MESSAGE);
        mostrarClase(Integer.parseInt(clase.replaceAll("\\D+", "")));
    }

    private TareaAsignada buscarTareaPorClase(String clase) {
        for (TareaAsignada tarea : EstudiantePanel.tareasAsignadas) {
            if (tarea != null && tarea.clase != null && tarea.clase.equalsIgnoreCase(clase)) return tarea;
        }
        return null;
    }

    private TareaEntregada buscarEntregaPorClase(String clase) {
        for (TareaEntregada entrega : EstudiantePanel.tareasEntregadas) {
            if (entrega != null && entrega.clase != null && entrega.clase.equalsIgnoreCase(clase)) return entrega;
        }
        return null;
    }

    private boolean esEnlace(String recurso) {
        if (recurso == null) return false;
        String lower = recurso.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private void abrirRecurso(String recurso) {
        try {
            if (recurso == null || recurso.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay archivo o enlace asociado.", "Sin recurso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "La apertura de archivos o enlaces no esta soportada.", "Soporte no disponible", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (esEnlace(recurso)) {
                Desktop.getDesktop().browse(new URI(recurso));
                return;
            }
            File archivo = new File(recurso);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo no existe:\n" + recurso, "Archivo no encontrado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().open(archivo);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el recurso: " + ex.getMessage(), "Error al abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
