package vista;

import controlador.ControladorCategorias;
import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import excepciones.FechaInvalidaException;
import excepciones.PrioridadInvalidaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import vista.estilos.BotonModerno;
import vista.estilos.Tema;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.time.LocalDate;
import java.util.Enumeration;

/*
 * Formulario del arbol de categorias.
 *   Izquierda: JTree con las categorias y el total de tareas de cada rama.
 *   Derecha arriba: recorrido recursivo con sangria (texto).
 *   Derecha abajo: tareas de la rama seleccionada.
 * Toda la logica esta en el controlador / ArbolCategorias; aqui solo se muestra.
 */
public class FormCategorias extends JDialog {

    private final ControladorCategorias controlador;

    private final JTree jTree = new JTree();
    private final JTextArea txtRecorrido = new JTextArea();
    private final DefaultListModel<Tarea> modeloTareas = new DefaultListModel<>();
    private final JList<Tarea> listaTareas = new JList<>(modeloTareas);
    private final JLabel lblRuta = new JLabel("Seleccione una categoria");
    private final JTextField txtBuscar = new JTextField(14);

    public FormCategorias(JFrame owner, ControladorCategorias controlador) {
        super(owner, "Arbol de categorias", true);
        this.controlador = controlador;

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setBackground(Tema.FONDO);
        contenido.setBorder(Tema.margen(18));
        contenido.add(crearEncabezado(), BorderLayout.NORTH);
        contenido.add(crearCuerpo(), BorderLayout.CENTER);
        contenido.add(crearBarraAcciones(), BorderLayout.SOUTH);
        setContentPane(contenido);

        configurarArbol();
        refrescar(controlador.getRaiz());

        setSize(1000, 640);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(owner);
    }

    // ====================== Construccion de la interfaz ======================

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Categorias");
        titulo.setFont(Tema.fuente(Font.BOLD, 22));
        titulo.setForeground(Tema.TEXTO);

        lblRuta.setFont(Tema.fuente(Font.PLAIN, 13));
        lblRuta.setForeground(Tema.TEXTO_SUAVE);

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(lblRuta);

        // Buscador
        txtBuscar.setFont(Tema.fuente(Font.PLAIN, 13));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        txtBuscar.addActionListener(e -> buscarCategoria());       // Enter tambien busca
        BotonModerno btnBuscar = BotonModerno.secundario("Buscar");
        btnBuscar.addActionListener(e -> buscarCategoria());

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buscador.setOpaque(false);
        buscador.add(txtBuscar);
        buscador.add(btnBuscar);

        panel.add(textos, BorderLayout.WEST);
        panel.add(buscador, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane crearCuerpo() {
        // Izquierda: arbol
        JPanel tarjetaArbol = Tema.tarjeta("Arbol de categorias");
        JScrollPane scrollArbol = new JScrollPane(jTree);
        scrollArbol.setBorder(null);
        tarjetaArbol.add(scrollArbol, BorderLayout.CENTER);

        // Derecha arriba: recorrido recursivo estilo consola
        txtRecorrido.setEditable(false);
        txtRecorrido.setFont(Tema.fuenteMono(13));
        txtRecorrido.setBackground(Tema.CONSOLA_FONDO);
        txtRecorrido.setForeground(Tema.CONSOLA_TEXTO);
        txtRecorrido.setBorder(Tema.margen(12));
        JPanel tarjetaRecorrido = Tema.tarjeta("Recorrido recursivo con sangria  ·  (tareas por rama)");
        JScrollPane scrollTexto = new JScrollPane(txtRecorrido);
        scrollTexto.setBorder(null);
        tarjetaRecorrido.add(scrollTexto, BorderLayout.CENTER);

        // Derecha abajo: tareas de la rama
        listaTareas.setCellRenderer(new RenderTarea());
        listaTareas.setFixedCellHeight(44);
        JPanel tarjetaTareas = Tema.tarjeta("Tareas de la rama seleccionada");
        JScrollPane scrollLista = new JScrollPane(listaTareas);
        scrollLista.setBorder(null);
        tarjetaTareas.add(scrollLista, BorderLayout.CENTER);

        JSplitPane derecha = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tarjetaRecorrido, tarjetaTareas);
        estilizarSplit(derecha);
        derecha.setResizeWeight(0.55);

        JSplitPane principal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tarjetaArbol, derecha);
        estilizarSplit(principal);
        principal.setResizeWeight(0.38);
        return principal;
    }

    private void estilizarSplit(JSplitPane split) {
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(12);
        split.setContinuousLayout(true);
    }

    private JPanel crearBarraAcciones() {
        BotonModerno btnAgregar = BotonModerno.primario("+  Subcategoria");
        BotonModerno btnTarea = BotonModerno.secundario("+  Tarea aqui");
        BotonModerno btnInfo = BotonModerno.secundario("Estadisticas");
        BotonModerno btnEliminar = BotonModerno.peligro("Eliminar");
        BotonModerno btnCerrar = BotonModerno.secundario("Cerrar");

        btnAgregar.addActionListener(e -> agregarSubcategoria());
        btnTarea.addActionListener(e -> agregarTarea());
        btnInfo.addActionListener(e -> mostrarEstadisticas());
        btnEliminar.addActionListener(e -> eliminarCategoria());
        btnCerrar.addActionListener(e -> dispose());

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(btnAgregar);
        izquierda.add(btnTarea);
        izquierda.add(btnInfo);
        izquierda.add(btnEliminar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(btnCerrar, BorderLayout.EAST);
        return barra;
    }

    private void configurarArbol() {
        jTree.setCellRenderer(new RenderCategoria());
        jTree.setRowHeight(28);
        jTree.setShowsRootHandles(true);
        jTree.putClientProperty("JTree.lineStyle", "None");
        jTree.setBorder(Tema.margen(4));

        // Al seleccionar: mostrar ruta y tareas de la rama
        jTree.addTreeSelectionListener(e -> mostrarSeleccion());
    }

    // ====================== Arbol visual ======================

    // RECURSION: copia nuestro arbol a los nodos que entiende el JTree
    private DefaultMutableTreeNode construirNodoVisual(NodoCategoria nodo) {
        DefaultMutableTreeNode visual = new DefaultMutableTreeNode(nodo);
        for (NodoCategoria hijo : nodo.getHijos()) {
            visual.add(construirNodoVisual(hijo));
        }
        return visual;
    }

    // Reconstruye todo y deja seleccionado el nodo indicado
    private void refrescar(NodoCategoria seleccionar) {
        DefaultMutableTreeNode raizVisual = construirNodoVisual(controlador.getRaiz());
        jTree.setModel(new DefaultTreeModel(raizVisual));
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        txtRecorrido.setText(controlador.recorridoConSangria());
        seleccionarEnArbol(raizVisual, seleccionar);
    }

    private void seleccionarEnArbol(DefaultMutableTreeNode raizVisual, NodoCategoria objetivo) {
        Enumeration<?> nodos = raizVisual.preorderEnumeration();
        while (nodos.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) nodos.nextElement();
            if (n.getUserObject() == objetivo) {
                TreePath path = new TreePath(n.getPath());
                jTree.setSelectionPath(path);
                jTree.scrollPathToVisible(path);
                return;
            }
        }
    }

    private NodoCategoria getSeleccionada() {
        TreePath path = jTree.getSelectionPath();
        if (path == null) return null;
        return (NodoCategoria) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
    }

    private void mostrarSeleccion() {
        NodoCategoria n = getSeleccionada();
        modeloTareas.clear();
        if (n == null) {
            lblRuta.setText("Seleccione una categoria");
            return;
        }
        lblRuta.setText(controlador.ruta(n) + "   ·   " + controlador.contarTareas(n) + " tarea(s) en la rama");
        for (Tarea t : controlador.tareasDeRama(n)) {
            modeloTareas.addElement(t);
        }
    }

    // ====================== Acciones (manejo de excepciones) ======================

    private void agregarSubcategoria() {
        NodoCategoria padre = getSeleccionada();
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva subcategoria" + (padre != null ? " dentro de \"" + padre + "\":" : ":"),
                "Nueva subcategoria", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null) return;                              // cancelo
        try {
            NodoCategoria nueva = controlador.agregarSubcategoria(padre, nombre);
            refrescar(nueva);
        } catch (CampoVacioException | CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    private void eliminarCategoria() {
        NodoCategoria n = getSeleccionada();
        if (n != null && !n.esRaiz()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar \"" + n + "\" y todas sus subcategorias?", "Confirmar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        try {
            NodoCategoria padre = (n != null) ? n.getPadre() : null;
            controlador.eliminarCategoria(n);
            refrescar(padre);
        } catch (CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    private void buscarCategoria() {
        try {
            NodoCategoria encontrada = controlador.buscar(txtBuscar.getText());
            refrescar(encontrada);
        } catch (CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    // Formulario rapido para crear una tarea dentro de la categoria seleccionada
    private void agregarTarea() {
        NodoCategoria categoria = getSeleccionada();
        if (categoria == null) {
            error("Seleccione la categoria de la tarea.");
            return;
        }

        JTextField txtNombre = new JTextField(20);
        JTextField txtDescripcion = new JTextField(20);
        JSpinner spPrioridad = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JTextField txtFecha = new JTextField(LocalDate.now().plusDays(7).toString());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(etiqueta("Nombre"));                  form.add(txtNombre);
        form.add(etiqueta("Descripcion"));             form.add(txtDescripcion);
        form.add(etiqueta("Prioridad (1 - 5)"));       form.add(spPrioridad);
        form.add(etiqueta("Fecha limite (AAAA-MM-DD)")); form.add(txtFecha);

        int r = JOptionPane.showConfirmDialog(this, form, "Nueva tarea en " + categoria,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        try {
            controlador.crearTareaEnCategoria(categoria, txtNombre.getText(), txtDescripcion.getText(),
                    (Integer) spPrioridad.getValue(), txtFecha.getText());
            refrescar(categoria);
        } catch (CategoriaException | FechaInvalidaException
                | PrioridadInvalidaException | CampoVacioException ex) {
            error(ex.getMessage());
        }
    }

    private void mostrarEstadisticas() {
        JOptionPane.showMessageDialog(this,
                "Categorias:  " + controlador.totalCategorias()
                        + "\nNiveles del arbol:  " + controlador.niveles()
                        + "\nTareas totales:  " + controlador.totalTareas(),
                "Estadisticas", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(Font.BOLD, 12));
        l.setForeground(Tema.TEXTO_SUAVE);
        return l;
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // ====================== Renderers (como se dibuja cada fila) ======================

    // Fila del arbol: cuadrito de color segun el nivel + nombre + contador de tareas
    private class RenderCategoria extends DefaultTreeCellRenderer {
        RenderCategoria() {
            setBackgroundSelectionColor(Tema.aclarar(Tema.PRIMARIO, 0.85));
            setBorderSelectionColor(null);
            setTextSelectionColor(Tema.TEXTO);
            setFont(Tema.fuente(Font.PLAIN, 14));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                        boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode visual = (DefaultMutableTreeNode) value;
            if (!(visual.getUserObject() instanceof NodoCategoria)) return this;

            NodoCategoria nodo = (NodoCategoria) visual.getUserObject();
            Color color = Tema.ACENTOS[visual.getLevel() % Tema.ACENTOS.length];
            int cantidad = controlador.contarTareas(nodo);

            setText("<html>" + nodo.getNombre()
                    + "&nbsp;&nbsp;<font color='#9ca3af'>" + cantidad + "</font></html>");
            setIcon(new IconoColor(color, nodo.esHoja()));
            setFont(nodo.esRaiz() ? Tema.fuente(Font.BOLD, 14) : Tema.fuente(Font.PLAIN, 14));
            return this;
        }
    }

    // Icono simple: cuadrado relleno (tiene hijos) o con contorno (hoja)
    private static class IconoColor implements Icon {
        private final Color color;
        private final boolean hoja;

        IconoColor(Color color, boolean hoja) { this.color = color; this.hoja = hoja; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            if (hoja) {
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x + 1, y + 1, 10, 10, 4, 4);
            } else {
                g2.fillRoundRect(x, y, 12, 12, 4, 4);
            }
            g2.dispose();
        }

        @Override public int getIconWidth()  { return 14; }
        @Override public int getIconHeight() { return 12; }
    }

    // Fila de la lista de tareas: nombre + prioridad, fecha, categoria y estado
    private static class RenderTarea extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Tarea t = (Tarea) value;
            String estado = t.isCompletada()
                    ? "<font color='#16a34a'>Completada</font>"
                    : "<font color='#d97706'>Pendiente</font>";
            setText("<html><b>" + t.getNombre() + "</b><br>"
                    + "<font color='#6b7280'>Prioridad " + t.getPrioridad()
                    + "  ·  vence " + t.getFechaLimite()
                    + "  ·  " + t.getCategoria() + "  ·  </font>" + estado + "</html>");
            setFont(Tema.fuente(Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                setBackground(Tema.aclarar(Tema.PRIMARIO, 0.85));
                setForeground(Tema.TEXTO);
            }
            return this;
        }
    }
}