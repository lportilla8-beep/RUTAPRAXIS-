import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class AdminPanel extends JPanel {

    private CardLayout adminCardLayout;
    private JPanel adminContentPanel;
    
    private DefaultTableModel tableModel;
    private JTable userTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cbRoleFilter;
    
    private JPanel rightPreviewPanel;

    public AdminPanel() {
        setLayout(new BorderLayout());

        // Barra superior
        JPanel top = new ModernTopBar("RUTA PRAXIS - ADMINISTRADOR");

        // Sidebar
        JPanel sidebar = createSidebar();

        // Panel de Contenido Principal (CardLayout)
        adminCardLayout = new CardLayout();
        adminContentPanel = new JPanel(adminCardLayout);

        adminContentPanel.add(createAdminInicioPanel(), "INICIO");

        add(top, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(adminContentPanel, BorderLayout.CENTER);

        adminCardLayout.show(adminContentPanel, "INICIO");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(12, 43, 102));
        sidebar.setPreferredSize(new Dimension(250, 0));

        addSidebarButton(sidebar, "\u2302  Inicio", e -> {
            mostrarInicioActualizado();
        });

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String text, ActionListener listener) {
        boolean destacado = text.toLowerCase().startsWith("cerrar");
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, destacado ? 58 : 48));
        btn.setPreferredSize(new Dimension(250, destacado ? 58 : 48));
        btn.setBackground(destacado ? new Color(12, 43, 102) : new Color(30, 64, 175));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(destacado ? SwingConstants.CENTER : SwingConstants.LEFT);
        btn.setBorder(destacado
                ? BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(8, 14, 14, 14),
                    BorderFactory.createLineBorder(new Color(59, 130, 246), 1)
                )
                : BorderFactory.createEmptyBorder(0, 20, 0, 0));
        btn.setFont(new Font("Arial", Font.BOLD, destacado ? 15 : 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(destacado ? new Color(12, 43, 102) : new Color(37, 99, 235));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(destacado ? new Color(12, 43, 102) : new Color(30, 64, 175));
            }
        });
        
        if (listener != null) btn.addActionListener(listener);
        sidebar.add(btn);
    }

    // ====================== PANEL INICIO (GESTIÓN DE USUARIOS) ======================
    private JPanel createAdminInicioPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 250, 252)); // Slate 50

        // Barra de Herramientas Superior: Búsqueda y Filtro
        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Búsqueda
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblSearch = new JLabel("Buscar: ");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        lblSearch.setForeground(new Color(30, 64, 175));
        filterBar.add(lblSearch, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtSearch = new JTextField();
        PlaceholderSupport.apply(txtSearch, "Buscar usuario");
        txtSearch.setPreferredSize(new Dimension(250, 32));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        filterBar.add(txtSearch, gbc);

        // Filtro Rol
        gbc.gridx = 2; gbc.weightx = 0.0;
        JLabel lblRole = new JLabel("Rol: ");
        lblRole.setFont(new Font("Arial", Font.BOLD, 13));
        lblRole.setForeground(new Color(30, 64, 175));
        filterBar.add(lblRole, gbc);

        gbc.gridx = 3; gbc.weightx = 0.5;
        cbRoleFilter = new JComboBox<>(new String[]{
            "Todos", "TUTOR ACADÉMICO", "ESTUDIANTE", "ASESOR PEDAGÓGICO", "ADMINISTRADOR"
        });
        cbRoleFilter.setPreferredSize(new Dimension(180, 32));
        cbRoleFilter.setBackground(Color.WHITE);
        cbRoleFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        filterBar.add(cbRoleFilter, gbc);

        gbc.gridx = 4; gbc.weightx = 0.0;
        JButton btnNuevoUsuario = new JButton("Nuevo usuario");
        btnNuevoUsuario.setPreferredSize(new Dimension(135, 32));
        btnNuevoUsuario.setBackground(new Color(12, 43, 102));
        btnNuevoUsuario.setForeground(Color.WHITE);
        btnNuevoUsuario.setFocusPainted(false);
        btnNuevoUsuario.setBorder(BorderFactory.createEmptyBorder());
        btnNuevoUsuario.addActionListener(e -> abrirNuevoUsuario());
        filterBar.add(btnNuevoUsuario, gbc);

        panel.add(filterBar, BorderLayout.NORTH);

        // Tabla de Usuarios
        String[] columnNames = {"ID", "Nombre Completo", "Correo", "Rol", "Último Acceso", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Lectura
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(38);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setShowGrid(true);
        userTable.setGridColor(new Color(226, 232, 240));
        userTable.removeColumn(userTable.getColumnModel().getColumn(0));
        
        // Sorter para filtros
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Renderizadores de cabecera y celdas
        userTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(12, 43, 102));
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Arial", Font.BOLD, 12));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(30, 64, 175)));
                return l;
            }
        });

        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                    c.setForeground(new Color(12, 43, 102));
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 250, 252));
                    }
                    c.setForeground(new Color(30, 64, 175));
                }
                
                // Alineación
                int modelColumn = table.convertColumnIndexToModel(column);
                if (modelColumn == 0 || modelColumn == 4 || modelColumn == 5) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                // Color para el estado
                if (modelColumn == 5) {
                    String status = (String) value;
                    if (status.equalsIgnoreCase("Activo")) {
                        setForeground(new Color(21, 128, 61)); // verde-700
                    } else {
                        setForeground(new Color(185, 28, 28)); // rojo-700
                    }
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de Vista Previa y Acciones (Derecha)
        rightPreviewPanel = new JPanel(new BorderLayout());
        rightPreviewPanel.setPreferredSize(new Dimension(320, 0));
        rightPreviewPanel.setBackground(new Color(241, 245, 249)); // Slate 100
        rightPreviewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.add(rightPreviewPanel, BorderLayout.EAST);

        // Listeners para filtros de búsqueda
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        cbRoleFilter.addActionListener(e -> applyFilter());

        // Listener de selección en la tabla
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = userTable.convertRowIndexToModel(selectedRow);
                    String id = (String) tableModel.getValueAt(modelRow, 0);
                    Usuario seleccionado = buscarUsuarioPorId(id);
                    actualizarPreview(seleccionado);
                } else {
                    actualizarPreview(null);
                }
            }
        });

        // Cargar datos iniciales
        cargarUsuarios();

        return panel;
    }

    private void applyFilter() {
        String text = PlaceholderSupport.getText(txtSearch).trim();
        String role = (String) cbRoleFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rfText = null;
        RowFilter<DefaultTableModel, Object> rfRole = null;

        if (!text.isEmpty()) {
            rfText = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2); // Nombre, Correo
        }

        if (role != null && !role.equals("Todos")) {
            String roleFilter = normalizarTexto(role);
            rfRole = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    Object value = entry.getValue(3);
                    return normalizarTexto(value != null ? value.toString() : "").equals(roleFilter);
                }
            };
        }

        java.util.List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();
        if (rfText != null) filters.add(rfText);
        if (rfRole != null) filters.add(rfRole);

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{M}", "");
        return normalizado.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private String obtenerIniciales(String nombreCompleto) {
        String[] partes = textoSeguro(nombreCompleto).trim().split("\\s+");
        String iniciales = "";
        if (partes.length > 0 && !partes[0].isEmpty()) iniciales += partes[0].substring(0, 1).toUpperCase();
        if (partes.length > 1 && !partes[1].isEmpty()) iniciales += partes[1].substring(0, 1).toUpperCase();
        return iniciales;
    }

    private Usuario buscarUsuarioPorId(String id) {
        for (Usuario u : UserData.usuarios) {
            if (u != null && textoSeguro(u.id).equals(textoSeguro(id))) {
                return u;
            }
        }
        return null;
    }

    public void recargarUsuariosDesdeBD() {
        ConexionBD.inicializarTablas();
        if (ConexionBD.isUsandoBD()) {
            UserData.usuarios.clear();
            UserData.usuarios.addAll(ConexionBD.cargarUsuarios());
        }
        cargarUsuarios();
        applyFilter();
    }

    public void mostrarInicioActualizado() {
        ConexionBD.inicializarTablas();
        if (ConexionBD.isUsandoBD()) {
            UserData.usuarios.clear();
            UserData.usuarios.addAll(ConexionBD.cargarUsuarios());
        }

        if (txtSearch != null) {
            txtSearch.setText("");
            txtSearch.putClientProperty("placeholder.active", false);
        }
        if (cbRoleFilter != null) {
            cbRoleFilter.setSelectedItem("Todos");
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
            sorter.setSortKeys(null);
        }

        cargarUsuarios();
        if (userTable != null) {
            userTable.clearSelection();
            if (userTable.getRowCount() > 0) {
                userTable.scrollRectToVisible(userTable.getCellRect(0, 0, true));
            }
        }
        actualizarPreview(null);
        adminCardLayout.show(adminContentPanel, "INICIO");
        revalidate();
        repaint();
    }

    private void cargarUsuarios() {
        tableModel.setRowCount(0);
        for (Usuario u : UserData.usuarios) {
            if (u == null) continue;
            u.actualizarEstadoPorUltimoAcceso();
            tableModel.addRow(new Object[]{
                textoSeguro(u.id), textoSeguro(u.nombreCompleto), textoSeguro(u.correo), textoSeguro(u.rol), textoSeguro(u.ultimoAcceso), textoSeguro(u.estado)
            });
        }
        userTable.clearSelection();
        actualizarPreview(null);
    }

    // ====================== RENDERIZADO DEL PANEL DE ACCIONES (DERECHA) ======================
    private void actualizarPreview(Usuario u) {
        rightPreviewPanel.removeAll();

        if (u == null) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel lblInfo = new JLabel("<html><center><font color='#64748B'>Seleccione un usuario de la lista<br>para gestionar su perfil y realizar acciones.</font></center></html>");
            lblInfo.setFont(new Font("Arial", Font.ITALIC, 13));
            emptyPanel.add(lblInfo);
            rightPreviewPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
            ));

            // Avatar
            String nombre = textoSeguro(u.nombreCompleto);
            String rol = textoSeguro(u.rol);
            String estado = textoSeguro(u.estado);
            JPanel avatar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(12, 43, 102)); // Slate 800
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(64, 64));
            avatar.setMaximumSize(new Dimension(64, 64));
            avatar.setLayout(new GridBagLayout());
            
            JLabel initialsLabel = new JLabel(obtenerIniciales(nombre));
            initialsLabel.setFont(new Font("Arial", Font.BOLD, 22));
            initialsLabel.setForeground(Color.WHITE);
            avatar.add(initialsLabel);
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Nombre
            JLabel lblName = new JLabel("<html><div style='text-align: center; width: 220px;'><b>" + nombre + "</b></div></html>", SwingConstants.CENTER);
            lblName.setFont(new Font("Arial", Font.BOLD, 15));
            lblName.setForeground(new Color(12, 43, 102));
            lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblName.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

            // Rol Badge
            Color rolBg = new Color(241, 245, 249);
            Color rolText = new Color(30, 64, 175);
            if (rol.contains("TUTOR")) {
                rolBg = new Color(219, 234, 254); rolText = new Color(29, 78, 216);
            } else if (rol.contains("ESTUDIANTE")) {
                rolBg = new Color(220, 252, 231); rolText = new Color(21, 128, 61);
            } else if (rol.contains("ASESOR")) {
                rolBg = new Color(243, 232, 255); rolText = new Color(126, 34, 206);
            } else if (rol.contains("ADMINISTRADOR")) {
                rolBg = new Color(254, 226, 226); rolText = new Color(185, 28, 28);
            }
            AdminStatusBadge rolBadge = new AdminStatusBadge(rol, rolBg, rolText);
            rolBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Separador
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(226, 232, 240));
            sep.setMaximumSize(new Dimension(240, 2));
            sep.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

            // Información Detallada
            JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 8));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setMaximumSize(new Dimension(240, 100));

            infoPanel.add(crearDetalleLabel("Correo: ", textoSeguro(u.correo)));
            infoPanel.add(crearDetalleLabel("Último Acceso: ", u.ultimoAcceso));
            
            // Estado Badge
            JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            statusRow.setBackground(Color.WHITE);
            JLabel titleLabel = new JLabel("Estado: ");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
            titleLabel.setForeground(new Color(100, 116, 139));
            statusRow.add(titleLabel);
            
            AdminStatusBadge estBadge = estado.equalsIgnoreCase("Activo")
                ? new AdminStatusBadge("ACTIVO", new Color(220, 252, 231), new Color(21, 128, 61))
                : new AdminStatusBadge("INACTIVO", new Color(254, 226, 226), new Color(185, 28, 28));
            statusRow.add(estBadge);
            infoPanel.add(statusRow);

            // Botones de Acción
            JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
            actionsPanel.setBackground(Color.WHITE);
            actionsPanel.setMaximumSize(new Dimension(240, 125));
            actionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            ModernAdminButton btnEdit = new ModernAdminButton("Editar Información ✎", new Color(37, 99, 235), new Color(29, 78, 216));
            ModernAdminButton btnProfile = new ModernAdminButton("Ver Perfil Completo 👤", new Color(21, 128, 61), new Color(22, 101, 52));
            ModernAdminButton btnReset = new ModernAdminButton("Restablecer Contraseña ⟲", new Color(217, 119, 6), new Color(180, 83, 9));

            btnEdit.addActionListener(e -> abrirEditorUsuario(u));
            btnProfile.addActionListener(e -> abrirVerPerfil(u));
            btnReset.addActionListener(e -> restablecerUsuario(u));

            actionsPanel.add(btnEdit);
            actionsPanel.add(btnProfile);
            actionsPanel.add(btnReset);

            card.add(avatar);
            card.add(lblName);
            card.add(rolBadge);
            card.add(sep);
            card.add(infoPanel);
            card.add(actionsPanel);
            card.add(Box.createVerticalGlue());

            rightPreviewPanel.add(card, BorderLayout.CENTER);
        }

        rightPreviewPanel.revalidate();
        rightPreviewPanel.repaint();
    }

    private JPanel crearDetalleLabel(String titulo, String valor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));
        
        JLabel lblValue = new JLabel(valor);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        lblValue.setForeground(new Color(30, 64, 175));

        p.add(lblTitle);
        p.add(lblValue);
        return p;
    }

    // ====================== DIÁLOGOS DE ACCIONES ======================
    
    // 1. EDITAR INFORMACIÓN
    private void abrirNuevoUsuario() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topFrame, "Nuevo Usuario", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(topFrame);
        dialog.setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        container.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtNombre = new JTextField();
        txtNombre.setPreferredSize(new Dimension(0, 32));
        container.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        container.add(new JLabel("Correo Institucional:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtCorreo = new JTextField();
        txtCorreo.setPreferredSize(new Dimension(0, 32));
        container.add(txtCorreo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        container.add(new JLabel("Rol asignado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cbRol = new JComboBox<>(new String[]{
            "TUTOR ACADEMICO", "ESTUDIANTE", "ASESOR PEDAGOGICO", "ADMINISTRADOR"
        });
        cbRol.setBackground(Color.WHITE);
        cbRol.setPreferredSize(new Dimension(0, 32));
        container.add(cbRol, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        container.add(new JLabel("Contrasena:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPasswordField txtContrasena = new JPasswordField();
        txtContrasena.setPreferredSize(new Dimension(0, 32));
        JPanel passwordPanel = new JPanel(new BorderLayout(6, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(txtContrasena, BorderLayout.CENTER);
        passwordPanel.add(createPasswordToggleButton(txtContrasena), BorderLayout.EAST);
        container.add(passwordPanel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 34));
        btnCancelar.setBackground(new Color(241, 245, 249));
        btnCancelar.setForeground(new Color(30, 64, 175));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = new JButton("Crear Usuario");
        btnGuardar.setPreferredSize(new Dimension(130, 34));
        btnGuardar.setBackground(new Color(12, 43, 102));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String cor = txtCorreo.getText().trim();
            String pass = new String(txtContrasena.getPassword()).trim();
            String rol = (String) cbRol.getSelectedItem();

            if (nom.isEmpty() || cor.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Por favor complete todos los campos obligatorios.", "Campos vacios", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario nuevo = new Usuario("", nom, cor, rol, "", "Inactivo", pass);
            ConexionBD.guardarUsuario(nuevo);
            JOptionPane.showMessageDialog(dialog, "Usuario creado correctamente.", "Usuario creado", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            recargarUsuariosDesdeBD();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        container.add(btnPanel, gbc);

        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void abrirEditorUsuario(Usuario u) {
        String idAnterior = textoSeguro(u.id);
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topFrame, "Editar Usuario - " + idAnterior, true);
        dialog.setSize(460, 430);
        dialog.setLocationRelativeTo(topFrame);
        dialog.setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        container.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtNombre = new JTextField(textoSeguro(u.nombreCompleto));
        txtNombre.setPreferredSize(new Dimension(0, 32));
        container.add(txtNombre, gbc);

        // Correo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        container.add(new JLabel("Correo Institucional:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtCorreo = new JTextField(textoSeguro(u.correo));
        txtCorreo.setPreferredSize(new Dimension(0, 32));
        container.add(txtCorreo, gbc);

        // Rol
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        container.add(new JLabel("Rol asignado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cbRol = new JComboBox<>(new String[]{
            "TUTOR ACADÉMICO", "ESTUDIANTE", "ASESOR PEDAGÓGICO", "ADMINISTRADOR"
        });
        cbRol.setSelectedItem(textoSeguro(u.rol));
        cbRol.setBackground(Color.WHITE);
        cbRol.setPreferredSize(new Dimension(0, 32));
        container.add(cbRol, gbc);

        // Contrasena
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        container.add(new JLabel("Contrasena:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPasswordField txtContrasena = new JPasswordField(textoSeguro(u.contrasena));
        txtContrasena.setPreferredSize(new Dimension(0, 32));
        JPanel passwordPanel = new JPanel(new BorderLayout(6, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(txtContrasena, BorderLayout.CENTER);
        passwordPanel.add(createPasswordToggleButton(txtContrasena), BorderLayout.EAST);
        container.add(passwordPanel, gbc);

        // Estado
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        container.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        u.actualizarEstadoPorUltimoAcceso();
        cbEstado.setSelectedItem(u.estado);
        cbEstado.setBackground(Color.WHITE);
        cbEstado.setPreferredSize(new Dimension(0, 32));
        cbEstado.setEnabled(false);
        container.add(cbEstado, gbc);

        // Botones de acción del diálogo
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 34));
        btnCancelar.setBackground(new Color(241, 245, 249));
        btnCancelar.setForeground(new Color(30, 64, 175));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(140, 34));
        btnGuardar.setBackground(new Color(12, 43, 102));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String cor = txtCorreo.getText().trim();
            String pass = new String(txtContrasena.getPassword()).trim();
            String rl = (String) cbRol.getSelectedItem();
            if (nom.isEmpty() || cor.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Por favor complete todos los campos obligatorios.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            u.nombreCompleto = nom;
            u.correo = cor;
            u.rol = rl;
            u.contrasena = pass;
            u.actualizarEstadoPorUltimoAcceso();
            ConexionBD.guardarUsuario(u, idAnterior); // Guardar en BD

            JOptionPane.showMessageDialog(dialog, "✅ Información de usuario actualizada correctamente.", "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            recargarUsuariosDesdeBD();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        container.add(btnPanel, gbc);

        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // 2. VER PERFIL COMPLETO
    private void abrirVerPerfil(Usuario u) {
        String nombre = textoSeguro(u.nombreCompleto);
        String rol = textoSeguro(u.rol);
        if (u.rol == null) u.rol = rol;
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topFrame, "Perfil del Usuario - " + nombre, true);
        dialog.setSize(460, 480);
        dialog.setLocationRelativeTo(topFrame);
        dialog.setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Iniciales grandes en círculo de avatar
        JPanel bigAvatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12, 43, 102)); // Slate 900
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bigAvatar.setPreferredSize(new Dimension(80, 80));
        bigAvatar.setMaximumSize(new Dimension(80, 80));
        bigAvatar.setLayout(new GridBagLayout());

        JLabel initialsLabel = new JLabel(obtenerIniciales(nombre));
        initialsLabel.setFont(new Font("Arial", Font.BOLD, 28));
        initialsLabel.setForeground(Color.WHITE);
        bigAvatar.add(initialsLabel);
        bigAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(nombre);
        lblName.setFont(new Font("Arial", Font.BOLD, 18));
        lblName.setForeground(new Color(12, 43, 102));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JLabel lblRolDesc = new JLabel(rol);
        lblRolDesc.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 12));
        lblRolDesc.setForeground(new Color(100, 116, 139));
        lblRolDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        sep.setMaximumSize(new Dimension(380, 2));
        sep.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Grid de Información detallada
        JPanel gridPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setMaximumSize(new Dimension(380, 180));

        gridPanel.add(crearFilaInfo("Correo Electrónico: ", u.correo));
        gridPanel.add(crearFilaInfo("Rol del Sistema: ", rol));
        gridPanel.add(crearFilaInfo("Fecha del Último Acceso: ", u.ultimoAcceso));
        gridPanel.add(crearFilaInfo("Estado de Cuenta: ", textoSeguro(u.estado)));
        gridPanel.add(crearFilaInfo("Clases Vinculadas: ", rol.contains("ESTUDIANTE") ? "5 Clases (Clase 1 a 5)" : (rol.contains("TUTOR") ? "5 Grupos Académicos" : "N/A")));

        JButton btnCerrar = new JButton("Cerrar Perfil");
        btnCerrar.setPreferredSize(new Dimension(140, 36));
        btnCerrar.setMaximumSize(new Dimension(140, 36));
        btnCerrar.setBackground(new Color(12, 43, 102));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder());
        btnCerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrar.addActionListener(e -> dialog.dispose());

        container.add(bigAvatar);
        container.add(lblName);
        container.add(lblRolDesc);
        container.add(sep);
        container.add(gridPanel);
        container.add(Box.createRigidArea(new Dimension(0, 20)));
        container.add(btnCerrar);

        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel crearFilaInfo(String clave, String valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel lblClave = new JLabel(clave);
        lblClave.setFont(new Font("Arial", Font.BOLD, 12));
        lblClave.setForeground(new Color(100, 116, 139));
        
        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Arial", Font.PLAIN, 12));
        lblVal.setForeground(new Color(12, 43, 102));

        p.add(lblClave, BorderLayout.WEST);
        p.add(lblVal, BorderLayout.EAST);
        return p;
    }

    private JButton createPasswordToggleButton(JPasswordField field) {
        char hiddenEcho = field.getEchoChar();
        JButton button = new JButton("👁");
        button.setToolTipText("Mostrar contrasena");
        button.setPreferredSize(new Dimension(36, 32));
        button.setFont(new Font("Dialog", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(30, 64, 175));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            boolean mostrar = field.getEchoChar() != 0;
            field.setEchoChar(mostrar ? (char) 0 : hiddenEcho);
            button.setToolTipText(mostrar ? "Ocultar contrasena" : "Mostrar contrasena");
        });
        return button;
    }

    // 3. RESTABLECER CONTRASEÑA
    private void restablecerUsuario(Usuario u) {
        int opt = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea restablecer la contraseña de " + u.nombreCompleto + "?\nEsta acción restablecerá el acceso a la contraseña temporal estándar.", 
            "Confirmar Restablecimiento", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (opt == JOptionPane.YES_OPTION) {
            u.contrasena = "123456";
            JOptionPane.showMessageDialog(this, 
                "✅ La contraseña de " + u.nombreCompleto + " ha sido restablecida con éxito.\nContraseña temporal asignada: 123456", 
                "Contraseña Restablecida", 
                JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
        }
    }

    // ====================== PANELES COMPLEMENTARIOS ======================
    private JPanel createGenericPanel(String titulo, String descripcion) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel lblTitle = new JLabel(titulo, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(lblTitle, gbc);

        gbc.gridy = 1;
        JLabel lblDesc = new JLabel(descripcion, SwingConstants.CENTER);
        lblDesc.setFont(new Font("Arial", Font.ITALIC, 14));
        lblDesc.setForeground(Color.GRAY);
        panel.add(lblDesc, gbc);

        return panel;
    }

    // ====================== COMPONENTES INTERNOS AUXILIARES ======================
    private static class AdminStatusBadge extends JPanel {
        private Color bgColor;
        private Color textColor;

        public AdminStatusBadge(String text, Color bgColor, Color textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
            setOpaque(false);
            setLayout(new BorderLayout());
            
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 9));
            label.setForeground(textColor);
            label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ModernAdminButton extends JButton {
        private Color normalColor;
        private Color hoverColor;

        public ModernAdminButton(String text, Color normalColor, Color hoverColor) {
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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
