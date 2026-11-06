package vista;

import controlador.ControladorCategorias;
import controlador.ControladorTareas;
import controlador.ControladorTareas.Filtro;
import controlador.ControladorTareas.Orden;
import excepciones.CampoVacioException;
import excepciones.CategoriaException;
import excepciones.CategoriaNoEncontradaException;
import excepciones.ColaVaciaException;
import excepciones.FechaInvalidaException;
import excepciones.HistorialVacioException;
import excepciones.PrioridadInvalidaException;
import excepciones.TareaNoEncontradaException;
import modelo.entidades.NodoCategoria;
import modelo.entidades.Tarea;
import modelo.entidades.TareaEnEspera;
import vista.estilos.BotonModerno;
import vista.estilos.Tema;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.List;


public class VentanaPrincipal extends JFrame {

    private final ControladorCategorias ctrlCategorias;
    private final ControladorTareas ctrlTareas;

    // Arbol
    private final JTree jTree = new JTree();

    // Encabezado y panel de tareas
    private final JLabel lblResumen = new JLabel();
    private final JLabel lblCategoria = new JLabel();
    private final JLabel lblRuta = new JLabel();
    private final JLabel lblMensaje = new JLabel(" ");
    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cbTipoBusqueda = new JComboBox<>(new String[]{"Tarea", "Categoría"});
    private final JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Todas", "Pendientes", "Completadas"});
    private final JComboBox<String> cbOrden = new JComboBox<>(new String[]{
            "Sin ordenar", "Fecha: más próxima", "Fecha: más lejana", "Prioridad: 1 → 5", "Prioridad: 5 → 1"});

    // Lista de tareas
    private final DefaultListModel<Tarea> modeloTareas = new DefaultListModel<>();
    private final JList<Tarea> listaTareas = new JList<>(modeloTareas);

    // Cola de tareas en espera
    private final DefaultListModel<TareaEnEspera> modeloCola = new DefaultListModel<>();
    private final JList<TareaEnEspera> listaCola = new JList<>(modeloCola);
    private final JLabel lblCola = new JLabel(" ");

    // Botones que cambian de texto
    private final BotonModerno btnCompletar = BotonModerno.secundario("Completar");
    private final BotonModerno btnDeshacer = BotonModerno.secundario("Deshacer");

    public VentanaPrincipal(ControladorCategorias ctrlCategorias, ControladorTareas ctrlTareas) {
        super("Gestor de Tareas");
        this.ctrlCategorias = ctrlCategorias;
        this.ctrlTareas = ctrlTareas;

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(Tema.FONDO);
        cuerpo.setBorder(Tema.margen(16));
        cuerpo.add(crearCuerpo(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);

        configurarArbol();
        configurarLista();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
        });

        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);

        refrescar(ctrlCategorias.getRaiz(), null);
    }


    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Tema.PRIMARIO, getWidth(), getHeight(), Tema.SECUNDARIO));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        encabezado.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("Gestor de Tareas");
        titulo.setFont(Tema.fuente(Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("To-Do List Avanzado  ·  Estructuras de Datos");
        subtitulo.setFont(Tema.fuente(Font.PLAIN, 13));
        subtitulo.setForeground(Tema.aclarar(Tema.PRIMARIO, 0.75));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        lblResumen.setFont(Tema.fuente(Font.BOLD, 13));
        lblResumen.setForeground(Color.WHITE);

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(lblResumen, BorderLayout.EAST);
        return encabezado;
    }

    private JSplitPane crearCuerpo() {
        // Izquierda: arbol arriba y cola abajo
        JSplitPane izquierda = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelCategorias(), crearPanelCola());
        izquierda.setBorder(null);
        izquierda.setOpaque(false);
        izquierda.setDividerSize(14);
        izquierda.setResizeWeight(0.62);
        izquierda.setContinuousLayout(true);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, crearPanelTareas());
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(14);
        split.setResizeWeight(0.28);
        split.setContinuousLayout(true);
        return split;
    }

    // Panel izquierdo: categorias 

    private JPanel crearPanelCategorias() {
        JPanel tarjeta = Tema.tarjeta("Categorías");

        JScrollPane scroll = new JScrollPane(jTree);
        scroll.setBorder(null);
        tarjeta.add(scroll, BorderLayout.CENTER);

        BotonModerno btnNueva = BotonModerno.primario("+ Subcategoría");
        BotonModerno btnEliminar = BotonModerno.peligro("Eliminar");
        btnNueva.addActionListener(e -> agregarSubcategoria());
        btnEliminar.addActionListener(e -> eliminarCategoria());

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.setOpaque(false);
        botones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        botones.add(btnNueva);
        botones.add(btnEliminar);
        tarjeta.add(botones, BorderLayout.SOUTH);

        tarjeta.setMinimumSize(new Dimension(250, 200));
        return tarjeta;
    }

    //  Panel izquierdo abajo: cola de espera 

    private JPanel crearPanelCola() {
        JPanel tarjeta = Tema.tarjeta("Bandeja de entrada  ·  Cola");

        // La cola se atiende en orden: la lista solo se muestra, no se selecciona
        listaCola.setCellRenderer(new RenderCola());
        listaCola.setFixedCellHeight(44);
        listaCola.setFocusable(false);
        listaCola.setSelectionModel(new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int a, int b) { }
            @Override public void addSelectionInterval(int a, int b) { }
        });

        lblCola.setFont(Tema.fuente(Font.PLAIN, 12));
        lblCola.setForeground(Tema.TEXTO_SUAVE);
        lblCola.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JScrollPane scroll = new JScrollPane(listaCola);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblCola, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        BotonModerno btnRecibir = BotonModerno.secundario("+ Crear tarea");
        BotonModerno btnAsignar = BotonModerno.primario("Asignar categoria");
        btnRecibir.addActionListener(e -> recibirTarea());
        btnAsignar.addActionListener(e -> asignarSiguiente());

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.setOpaque(false);
        botones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        botones.add(btnRecibir);
        botones.add(btnAsignar);

        tarjeta.add(centro, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        tarjeta.setMinimumSize(new Dimension(250, 150));
        return tarjeta;
    }

    //  Panel derecho: tareas 

    private JPanel crearPanelTareas() {
        JPanel tarjeta = Tema.tarjeta(null);

        // Titulo de la categoria seleccionada
        lblCategoria.setFont(Tema.fuente(Font.BOLD, 20));
        lblCategoria.setForeground(Tema.TEXTO);
        lblRuta.setFont(Tema.fuente(Font.PLAIN, 13));
        lblRuta.setForeground(Tema.TEXTO_SUAVE);

        JPanel titulo = new JPanel(new GridLayout(2, 1));
        titulo.setOpaque(false);
        titulo.add(lblCategoria);
        titulo.add(lblRuta);

        // Buscador (tarea por busqueda binaria, o categoria en el arbol)
        estilizarCampo(txtBuscar);
        txtBuscar.addActionListener(e -> buscar());
        estilizarCombo(cbTipoBusqueda);
        BotonModerno btnBuscar = BotonModerno.secundario("Buscar");
        btnBuscar.addActionListener(e -> buscar());

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buscador.setOpaque(false);
        buscador.add(txtBuscar);
        buscador.add(cbTipoBusqueda);
        buscador.add(btnBuscar);

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.add(titulo, BorderLayout.WEST);
        filaTitulo.add(buscador, BorderLayout.EAST);

        // Filtro y orden
        estilizarCombo(cbFiltro);
        estilizarCombo(cbOrden);
        cbFiltro.addActionListener(e -> cargarTareas(listaTareas.getSelectedValue()));
        cbOrden.addActionListener(e -> cargarTareas(listaTareas.getSelectedValue()));

        JPanel filaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaFiltros.setOpaque(false);
        filaFiltros.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        filaFiltros.add(etiqueta("Mostrar"));
        filaFiltros.add(cbFiltro);
        filaFiltros.add(Box.createHorizontalStrut(12));
        filaFiltros.add(etiqueta("Ordenar por"));
        filaFiltros.add(cbOrden);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(filaTitulo, BorderLayout.NORTH);
        arriba.add(filaFiltros, BorderLayout.SOUTH);

        // Lista
        JScrollPane scroll = new JScrollPane(listaTareas);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));

        // Acciones
        BotonModerno btnNueva = BotonModerno.primario("+ Nueva tarea");
        BotonModerno btnEditar = BotonModerno.secundario("Editar");
        BotonModerno btnEliminar = BotonModerno.peligro("Eliminar");

        btnNueva.addActionListener(e -> nuevaTarea());
        btnEditar.addActionListener(e -> editarTarea());
        btnCompletar.addActionListener(e -> completarOReabrir());
        btnEliminar.addActionListener(e -> eliminarTarea());
        btnDeshacer.addActionListener(e -> deshacer());

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(btnNueva);
        izquierda.add(btnEditar);
        izquierda.add(btnCompletar);
        izquierda.add(btnEliminar);

        lblMensaje.setFont(Tema.fuente(Font.PLAIN, 12));
        lblMensaje.setForeground(Tema.TEXTO_SUAVE);
        lblMensaje.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        derecha.add(lblMensaje);
        derecha.add(btnDeshacer);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        acciones.add(izquierda, BorderLayout.WEST);
        acciones.add(derecha, BorderLayout.EAST);

        tarjeta.add(arriba, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void configurarArbol() {
        jTree.setCellRenderer(new RenderCategoria());
        jTree.setRowHeight(30);
        jTree.setShowsRootHandles(true);
        jTree.putClientProperty("JTree.lineStyle", "None");
        jTree.setBorder(Tema.margen(4));
        jTree.addTreeSelectionListener(e -> cargarTareas(null));
    }

    private void configurarLista() {
        listaTareas.setCellRenderer(new RenderTarea());
        listaTareas.setFixedCellHeight(58);
        listaTareas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaTareas.addListSelectionListener(e -> actualizarBotonCompletar());

        // Doble clic sobre una tarea = editar
        listaTareas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && listaTareas.getSelectedValue() != null) editarTarea();
            }
        });
    }

    // Reconstruye el arbol y deja seleccionadas la categoria y la tarea indicadas
    private void refrescar(NodoCategoria categoria, Tarea tarea) {
        DefaultMutableTreeNode raizVisual = construirNodoVisual(ctrlCategorias.getRaiz());
        jTree.setModel(new DefaultTreeModel(raizVisual));
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
        seleccionarEnArbol(raizVisual, categoria != null ? categoria : ctrlCategorias.getRaiz());
        cargarTareas(tarea);
        cargarCola();
        actualizarResumen();
    }

    // Muestra la cola en orden de llegada (la primera es la siguiente en asignarse)
    private void cargarCola() {
        modeloCola.clear();
        for (TareaEnEspera t : ctrlTareas.tareasEnEspera()) {
            modeloCola.addElement(t);
        }
        int n = modeloCola.size();
        lblCola.setText(n == 0
                ? "No hay tareas en espera"
                : n + " en espera  ·  se asignan en orden de llegada");
    }

    // Recursion: convierte el arbol del modelo en nodos del JTree
    private DefaultMutableTreeNode construirNodoVisual(NodoCategoria nodo) {
        DefaultMutableTreeNode visual = new DefaultMutableTreeNode(nodo);
        for (NodoCategoria hijo : nodo.getHijos()) {
            visual.add(construirNodoVisual(hijo));
        }
        return visual;
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

    private NodoCategoria getCategoriaSeleccionada() {
        TreePath path = jTree.getSelectionPath();
        if (path == null) return null;
        return (NodoCategoria) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
    }

    // Llena la lista con las tareas de la categoria seleccionada (y sus subcategorias)
    private void cargarTareas(Tarea seleccionar) {
        modeloTareas.clear();
        NodoCategoria nodo = getCategoriaSeleccionada();
        if (nodo == null) {
            lblCategoria.setText("Seleccione una categoría");
            lblRuta.setText(" ");
            actualizarBotonCompletar();
            return;
        }

        Filtro filtro = Filtro.values()[cbFiltro.getSelectedIndex()];
        Orden orden = Orden.values()[cbOrden.getSelectedIndex()];
        List<Tarea> tareas = ctrlTareas.tareasParaMostrar(nodo, filtro, orden);
        for (Tarea t : tareas) {
            modeloTareas.addElement(t);
        }

        lblCategoria.setText(nodo.getNombre());
        lblRuta.setText(ctrlCategorias.ruta(nodo) + "   ·   " + ctrlCategorias.contarTareas(nodo)
                + " tarea(s) en esta rama");

        if (seleccionar != null && modeloTareas.contains(seleccionar)) {
            listaTareas.setSelectedValue(seleccionar, true);
        }
        actualizarBotonCompletar();
    }

    private void actualizarResumen() {
        lblResumen.setText("Pendientes: " + ctrlTareas.totalPendientes()
                + "     Completadas: " + ctrlTareas.totalCompletadas()
                + "     En espera: " + ctrlTareas.totalEnEspera()
                + "     Categorías: " + ctrlCategorias.totalCategorias());
        int acciones = ctrlTareas.accionesEnHistorial();
        btnDeshacer.setText(acciones == 0 ? "Deshacer" : "Deshacer (" + acciones + ")");
    }

    private void actualizarBotonCompletar() {
        Tarea t = listaTareas.getSelectedValue();
        btnCompletar.setText(t != null && t.isCompletada() ? "Reabrir" : "Completar");
    }

    private void avisar(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    private void agregarSubcategoria() {
        NodoCategoria padre = getCategoriaSeleccionada();
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva subcategoría" + (padre != null ? " dentro de \"" + padre + "\":" : ":"),
                "Nueva subcategoría", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null) return;
        try {
            NodoCategoria nueva = ctrlCategorias.agregarSubcategoria(padre, nombre);
            refrescar(nueva, null);
            avisar("Categoría \"" + nueva.getNombre() + "\" creada.");
        } catch (CampoVacioException | CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    private void eliminarCategoria() {
        NodoCategoria nodo = getCategoriaSeleccionada();
        if (nodo != null && !nodo.esRaiz()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar la categoría \"" + nodo + "\" y todas sus subcategorías?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        try {
            NodoCategoria padre = (nodo != null) ? nodo.getPadre() : null;
            ctrlCategorias.eliminarCategoria(nodo);
            refrescar(padre, null);
            avisar("Categoría eliminada.");
        } catch (CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    private void nuevaTarea() {
        mostrarFormularioTarea(null);
    }

    private void editarTarea() {
        Tarea t = listaTareas.getSelectedValue();
        if (t == null) {
            error("Seleccione la tarea que desea editar.");
            return;
        }
        mostrarFormularioTarea(t);
    }

    private void completarOReabrir() {
        Tarea t = listaTareas.getSelectedValue();
        try {
            if (t != null && t.isCompletada()) {
                ctrlTareas.reabrirTarea(t);
                avisar("\"" + t.getNombre() + "\" volvió a pendientes.");
            } else {
                ctrlTareas.completarTarea(t);
                avisar("\"" + t.getNombre() + "\" marcada como completada.");
            }
            refrescar(getCategoriaSeleccionada(), t);
        } catch (TareaNoEncontradaException ex) {
            error(ex.getMessage());
        }
    }

    private void eliminarTarea() {
        Tarea t = listaTareas.getSelectedValue();
        if (t != null) {
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar la tarea \"" + t.getNombre() + "\"?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        try {
            ctrlTareas.eliminarTarea(t);
            refrescar(getCategoriaSeleccionada(), null);
            avisar("Tarea eliminada. Puede usar Deshacer para recuperarla.");
        } catch (TareaNoEncontradaException ex) {
            error(ex.getMessage());
        }
    }

    private void deshacer() {
        try {
            String resultado = ctrlTareas.deshacer();
            refrescar(getCategoriaSeleccionada(), listaTareas.getSelectedValue());
            avisar(resultado);
        } catch (HistorialVacioException | TareaNoEncontradaException | CategoriaNoEncontradaException ex) {
            refrescar(getCategoriaSeleccionada(), null);
            error(ex.getMessage());
        }
    }

    // Formulario sin categoria: la tarea entra al final de la cola
    private void recibirTarea() {
        JTextField txtNombre = new JTextField(26);
        JTextField txtDescripcion = new JTextField(26);
        JSpinner spPrioridad = new JSpinner(new SpinnerNumberModel(3, Tarea.PRIORIDAD_MIN, Tarea.PRIORIDAD_MAX, 1));
        JTextField txtFecha = new JTextField(LocalDate.now().plusDays(7).toString());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(etiqueta("Nombre"));                      form.add(txtNombre);
        form.add(etiqueta("Descripción"));                 form.add(txtDescripcion);
        form.add(etiqueta("Prioridad (1 = más alta)"));    form.add(spPrioridad);
        form.add(etiqueta("Fecha límite (AAAA-MM-DD)"));   form.add(txtFecha);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, form, "Recibir tarea (sin categoría)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            try {
                TareaEnEspera recibida = ctrlTareas.recibirTarea(txtNombre.getText(), txtDescripcion.getText(),
                        (Integer) spPrioridad.getValue(), txtFecha.getText());
                cargarCola();
                actualizarResumen();
                avisar("\"" + recibida.getNombre() + "\" quedó en espera (posición " + ctrlTareas.totalEnEspera() + ").");
                return;
            } catch (CampoVacioException | PrioridadInvalidaException | FechaInvalidaException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void asignarSiguiente() {
        try {
            TareaEnEspera siguiente = ctrlTareas.verSiguiente();

            // Datos de la tarea que se va a asignar
            JLabel lblNombre = new JLabel(siguiente.getNombre());
            lblNombre.setFont(Tema.fuente(Font.BOLD, 15));
            lblNombre.setForeground(Tema.TEXTO);
            JLabel lblDatos = new JLabel("Prioridad " + siguiente.getPrioridad()
                    + "   ·   Vence " + siguiente.getFechaLimite()
                    + (siguiente.getDescripcion().isEmpty() ? "" : "   ·   " + siguiente.getDescripcion()));
            lblDatos.setFont(Tema.fuente(Font.PLAIN, 12));
            lblDatos.setForeground(Tema.TEXTO_SUAVE);

            // Combo de categorias (sin la raiz); se propone la seleccionada en el arbol
            JComboBox<NodoCategoria> cbCategoria = crearComboCategorias(false, getCategoriaSeleccionada());
            if (cbCategoria.getItemCount() == 0) {
                throw new CategoriaException("Primero cree al menos una categoría en el árbol.");
            }

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
            form.add(etiqueta("Tarea siguiente en la cola"));
            form.add(lblNombre);
            form.add(lblDatos);
            form.add(Box.createVerticalStrut(6));
            form.add(etiqueta("Asignar a la categoría"));
            form.add(cbCategoria);

            int r = JOptionPane.showConfirmDialog(this, form, "Asignar tarea de la cola",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;              // sigue en la cola

            NodoCategoria categoria = (NodoCategoria) cbCategoria.getSelectedItem();
            Tarea creada = ctrlTareas.asignarSiguiente(categoria);
            refrescar(categoria, creada);
            avisar("\"" + creada.getNombre() + "\" asignada a " + categoria.getNombre() + ".");
        } catch (FechaInvalidaException ex) {
            // La fecha vencio mientras esperaba: se ofrece sacarla de la cola
            int r = JOptionPane.showConfirmDialog(this,
                    ex.getMessage() + "\nLa fecha de esta tarea ya pasó. ¿Desea descartarla de la cola?",
                    "Tarea vencida", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    avisar("\"" + ctrlTareas.descartarSiguiente().getNombre() + "\" descartada de la cola.");
                } catch (ColaVaciaException ignorada) { }
                cargarCola();
                actualizarResumen();
            }
        } catch (ColaVaciaException | CategoriaException | PrioridadInvalidaException | CampoVacioException ex) {
            error(ex.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText();
        try {
            if (cbTipoBusqueda.getSelectedIndex() == 0) {
                // Tarea: busqueda binaria por nombre
                Tarea t = ctrlTareas.buscarPorNombre(texto);
                cbFiltro.setSelectedIndex(0);                    
                refrescar(ctrlTareas.categoriaDe(t), t);
                avisar("Tarea encontrada en " + t.getCategoria() + ".");
            } else {
                NodoCategoria n = ctrlCategorias.buscar(texto);
                refrescar(n, null);
                avisar("Categoría encontrada.");
            }
        } catch (TareaNoEncontradaException | CampoVacioException | CategoriaException ex) {
            error(ex.getMessage());
        }
    }

    private void mostrarFormularioTarea(Tarea existente) {
        boolean editando = existente != null;

        JTextField txtNombre = new JTextField(editando ? existente.getNombre() : "", 26);
        JTextField txtDescripcion = new JTextField(editando ? existente.getDescripcion() : "", 26);
        JSpinner spPrioridad = new JSpinner(new SpinnerNumberModel(
                editando ? existente.getPrioridad() : 3, Tarea.PRIORIDAD_MIN, Tarea.PRIORIDAD_MAX, 1));
        JTextField txtFecha = new JTextField(editando
                ? existente.getFechaLimite().toString()
                : LocalDate.now().plusDays(7).toString());

        JComboBox<NodoCategoria> cbCategoria = new JComboBox<>();
        for (NodoCategoria c : ctrlTareas.listarCategorias()) {
            cbCategoria.addItem(c);
        }
        cbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NodoCategoria) setText(((NodoCategoria) value).obtenerRutaCompleta());
                return this;
            }
        });
        NodoCategoria inicial = editando ? ctrlTareas.categoriaDe(existente) : getCategoriaSeleccionada();
        cbCategoria.setSelectedItem(inicial != null ? inicial : ctrlCategorias.getRaiz());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(etiqueta("Nombre"));                      form.add(txtNombre);
        form.add(etiqueta("Descripción"));                 form.add(txtDescripcion);
        form.add(etiqueta("Prioridad (1 = más alta)"));    form.add(spPrioridad);
        form.add(etiqueta("Fecha límite (AAAA-MM-DD)"));   form.add(txtFecha);
        form.add(etiqueta("Categoría"));                   form.add(cbCategoria);

        String titulo = editando ? "Editar tarea" : "Nueva tarea";
        while (true) {
            int r = JOptionPane.showConfirmDialog(this, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;

            NodoCategoria categoria = (NodoCategoria) cbCategoria.getSelectedItem();
            int prioridad = (Integer) spPrioridad.getValue();
            try {
                Tarea resultado;
                if (editando) {
                    ctrlTareas.editarTarea(existente, categoria, txtNombre.getText(), txtDescripcion.getText(),
                            prioridad, txtFecha.getText());
                    resultado = existente;
                    avisar("Tarea \"" + resultado.getNombre() + "\" actualizada.");
                } else {
                    resultado = ctrlTareas.crearTarea(categoria, txtNombre.getText(), txtDescripcion.getText(),
                            prioridad, txtFecha.getText());
                    avisar("Tarea \"" + resultado.getNombre() + "\" creada.");
                }
                // Si la categoria actual no contiene la tarea, se salta a la suya
                NodoCategoria mostrar = getCategoriaSeleccionada();
                if (mostrar == null || !ctrlCategorias.tareasDeRama(mostrar).contains(resultado)) {
                    mostrar = categoria;
                }
                refrescar(mostrar, resultado);
                return;
            } catch (TareaNoEncontradaException | CategoriaException | FechaInvalidaException
                        | PrioridadInvalidaException | CampoVacioException ex) {
                error(ex.getMessage());                 // y se vuelve a mostrar el formulario
            }
        }
    }

        /*
     * Combo con las categorias del arbol mostrando su ruta completa.
     * incluirRaiz = false deja fuera la categoria raiz.
     * Si "seleccionar" no esta en la lista, queda elegida la primera.
     */
    private JComboBox<NodoCategoria> crearComboCategorias(boolean incluirRaiz, NodoCategoria seleccionar) {
        JComboBox<NodoCategoria> combo = new JComboBox<>();
        for (NodoCategoria c : ctrlTareas.listarCategorias()) {
            if (incluirRaiz || !c.esRaiz()) combo.addItem(c);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NodoCategoria) setText(((NodoCategoria) value).obtenerRutaCompleta());
                return this;
            }
        });
        if (seleccionar != null && (incluirRaiz || !seleccionar.esRaiz())) {
            combo.setSelectedItem(seleccionar);
        }
        estilizarCombo(combo);
        return combo;
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea salir de la aplicación?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(Font.BOLD, 12));
        l.setForeground(Tema.TEXTO_SUAVE);
        return l;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(Tema.fuente(Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(Tema.fuente(Font.PLAIN, 13));
        combo.setBackground(Tema.SUPERFICIE);
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // Color segun la prioridad (1 = mas urgente)
    private static Color colorPrioridad(int prioridad) {
        switch (prioridad) {
            case 1:  return new Color(220, 38, 38);
            case 2:  return new Color(234, 88, 12);
            case 3:  return new Color(245, 158, 11);
            case 4:  return new Color(14, 165, 233);
            default: return new Color(148, 163, 184);
        }
    }


    // Fila del arbol: cuadrito de color por nivel + nombre + cantidad de tareas
    private class RenderCategoria extends DefaultTreeCellRenderer {
        RenderCategoria() {
            setBackgroundSelectionColor(Tema.aclarar(Tema.PRIMARIO, 0.85));
            setBorderSelectionColor(null);
            setTextSelectionColor(Tema.TEXTO);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                        boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode visual = (DefaultMutableTreeNode) value;
            if (!(visual.getUserObject() instanceof NodoCategoria)) return this;

            NodoCategoria nodo = (NodoCategoria) visual.getUserObject();
            Color color = Tema.ACENTOS[visual.getLevel() % Tema.ACENTOS.length];
            int cantidad = ctrlCategorias.contarTareas(nodo);

            setText("<html>" + nodo.getNombre()
                    + "&nbsp;&nbsp;<font color='#9ca3af'>" + cantidad + "</font></html>");
            setIcon(new IconoForma(color, !nodo.esHoja(), 12));
            setFont(nodo.esRaiz() ? Tema.fuente(Font.BOLD, 14) : Tema.fuente(Font.PLAIN, 14));
            return this;
        }
    }

    // Fila de tarea: punto de prioridad, nombre, detalles y estado
    private static class RenderTarea extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Tarea t = (Tarea) value;

            String estado;
            if (t.isCompletada()) {
                estado = "<font color='#16a34a'><b>Completada</b></font>";
            } else if (t.getFechaLimite().isBefore(LocalDate.now())) {
                estado = "<font color='#dc2626'><b>Vencida</b></font>";
            } else {
                estado = "<font color='#d97706'><b>Pendiente</b></font>";
            }

            String nombre = t.isCompletada()
                    ? "<font color='#9ca3af'><s>" + t.getNombre() + "</s></font>"
                    : "<b>" + t.getNombre() + "</b>";
            String descripcion = t.getDescripcion().isEmpty()
                    ? "" : "&nbsp;&nbsp;<font color='#9ca3af'>" + t.getDescripcion() + "</font>";

            setText("<html>" + nombre + descripcion + "<br>"
                    + "<font color='#6b7280'>Prioridad " + t.getPrioridad()
                    + "&nbsp;&nbsp;·&nbsp;&nbsp;Vence " + t.getFechaLimite()
                    + "&nbsp;&nbsp;·&nbsp;&nbsp;" + t.getCategoria() + "&nbsp;&nbsp;·&nbsp;&nbsp;</font>"
                    + estado + "</html>");
            setIcon(new IconoForma(colorPrioridad(t.getPrioridad()), true, 10));
            setIconTextGap(12);
            setFont(Tema.fuente(Font.PLAIN, 13));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            if (isSelected) {
                setBackground(Tema.aclarar(Tema.PRIMARIO, 0.85));
                setForeground(Tema.TEXTO);
            } else {
                setBackground(Tema.SUPERFICIE);
            }
            return this;
        }
    }

    // Fila de la cola: posicion, nombre y datos; la primera se marca como "Siguiente"
    private static class RenderCola extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, false, false);
            TareaEnEspera t = (TareaEnEspera) value;
            String marca = (index == 0) ? "&nbsp;&nbsp;<font color='#4f46e5'><b>Siguiente</b></font>" : "";
            setText("<html><b>" + (index + 1) + ".</b>&nbsp;" + t.getNombre() + marca + "<br>"
                    + "<font color='#6b7280'>Prioridad " + t.getPrioridad()
                    + "&nbsp;&nbsp;·&nbsp;&nbsp;Vence " + t.getFechaLimite() + "</font></html>");
            setIcon(new IconoForma(colorPrioridad(t.getPrioridad()), true, 8));
            setIconTextGap(10);
            setFont(Tema.fuente(Font.PLAIN, 12));
            setBackground(index == 0 ? Tema.aclarar(Tema.PRIMARIO, 0.9) : Tema.SUPERFICIE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)));
            return this;
        }
    }

    // Icono dibujado a mano: cuadrito redondeado relleno o solo con borde
    private static class IconoForma implements Icon {
        private final Color color;
        private final boolean relleno;
        private final int tam;

        IconoForma(Color color, boolean relleno, int tam) {
            this.color = color;
            this.relleno = relleno;
            this.tam = tam;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            if (relleno) {
                g2.fillRoundRect(x, y, tam, tam, tam / 2, tam / 2);
            } else {
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x + 1, y + 1, tam - 2, tam - 2, tam / 2, tam / 2);
            }
            g2.dispose();
        }

        @Override public int getIconWidth()  { return tam + 2; }
        @Override public int getIconHeight() { return tam; }
    }
}