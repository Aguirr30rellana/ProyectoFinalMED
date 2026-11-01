package vista;

import controlador.ControladorCategorias;
import modelo.servicios.ArbolCategorias;
import modelo.servicios.GestorTareas;
import vista.estilos.BotonModerno;
import vista.estilos.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * Menu principal: una tarjeta por modulo.
 * Para conectar un modulo nuevo solo se cambia la accion de su tarjeta, ej:
 *     () -> new FormTareas(this, gestor).setVisible(true)
 */
public class MenuPrincipal extends JFrame {

    // Datos compartidos por todos los formularios
    private final GestorTareas gestor;
    private final ArbolCategorias arbol;

    private final JLabel lblResumen = new JLabel();

    public MenuPrincipal(GestorTareas gestor, ArbolCategorias arbol) {
        super("Gestor de Tareas");
        this.gestor = gestor;
        this.arbol = arbol;

        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTarjetas(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
            // Al volver de un formulario se actualizan los contadores
            @Override public void windowActivated(WindowEvent e) { actualizarResumen(); }
        });

        setSize(760, 560);
        setMinimumSize(new Dimension(640, 500));
        setLocationRelativeTo(null);
        actualizarResumen();
    }

    //  Partes de la ventana 

    private JPanel crearEncabezado() {
        // Panel con degradado de color
        JPanel encabezado = new JPanel(new GridLayout(2, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Tema.PRIMARIO, getWidth(), getHeight(), Tema.SECUNDARIO));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        encabezado.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel titulo = new JLabel("Gestor de Tareas");
        titulo.setFont(Tema.fuente(Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("To-Do List Avanzado  ·  Proyecto de Estructuras de Datos");
        subtitulo.setFont(Tema.fuente(Font.PLAIN, 14));
        subtitulo.setForeground(Tema.aclarar(Tema.PRIMARIO, 0.75));

        encabezado.add(titulo);
        encabezado.add(subtitulo);
        return encabezado;
    }

    private JPanel crearTarjetas() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 16, 16));
        grid.setBackground(Tema.FONDO);
        grid.setBorder(BorderFactory.createEmptyBorder(22, 28, 10, 28));

        grid.add(crearTarjeta("Tareas", "Crear, completar y eliminar tareas",
                "ArrayList", Tema.ACENTOS[0], () -> pendiente("Tareas")));
        grid.add(crearTarjeta("Ordenar", "Por fecha limite o por prioridad",
                "Ordenamiento", Tema.ACENTOS[1], () -> pendiente("Ordenar")));
        grid.add(crearTarjeta("Deshacer", "Revertir la ultima accion realizada",
                "Pila", Tema.ACENTOS[2], () -> pendiente("Deshacer")));
        grid.add(crearTarjeta("Cola de asignacion", "Atender tareas en orden de llegada",
                "Cola", Tema.ACENTOS[3], () -> pendiente("Cola de asignacion")));
        grid.add(crearTarjeta("Categorias", "Arbol jerarquico con recorrido recursivo",
                "Arbol · Recursion", Tema.ACENTOS[4], this::abrirCategorias));
        grid.add(crearTarjeta("Buscar tarea", "Busqueda binaria por nombre",
                "Busqueda binaria", Tema.ACENTOS[5], () -> pendiente("Buscar tarea")));
        return grid;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Tema.FONDO);
        pie.setBorder(BorderFactory.createEmptyBorder(6, 28, 18, 28));

        lblResumen.setFont(Tema.fuente(Font.PLAIN, 13));
        lblResumen.setForeground(Tema.TEXTO_SUAVE);

        BotonModerno btnSalir = BotonModerno.secundario("Salir");
        btnSalir.addActionListener(e -> salir());

        pie.add(lblResumen, BorderLayout.CENTER);
        pie.add(btnSalir, BorderLayout.EAST);
        return pie;
    }

    // Tarjeta clicable con barra de color a la izquierda
    private JPanel crearTarjeta(String titulo, String descripcion, String estructura,
                                Color acento, Runnable accion) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Tema.SUPERFICIE);
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.setBorder(bordeTarjeta(acento, Tema.BORDE));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 16));
        lblTitulo.setForeground(Tema.TEXTO);

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(Tema.fuente(Font.PLAIN, 12));
        lblDesc.setForeground(Tema.TEXTO_SUAVE);

        // Etiqueta con la estructura de datos que usa el modulo
        JLabel chip = new JLabel(" " + estructura + " ");
        chip.setOpaque(true);
        chip.setBackground(Tema.aclarar(acento, 0.85));
        chip.setForeground(acento.darker());
        chip.setFont(Tema.fuente(Font.BOLD, 11));
        chip.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblDesc);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(chip);

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { tarjeta.setBorder(bordeTarjeta(acento, acento)); }
            @Override public void mouseExited(MouseEvent e)  { tarjeta.setBorder(bordeTarjeta(acento, Tema.BORDE)); }
            @Override public void mouseClicked(MouseEvent e) { accion.run(); }
        });
        return tarjeta;
    }

    private javax.swing.border.Border bordeTarjeta(Color acento, Color contorno) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(contorno),
                        BorderFactory.createMatteBorder(0, 5, 0, 0, acento)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    //  Acciones 

    private void abrirCategorias() {
        new FormCategorias(this, new ControladorCategorias(arbol, gestor)).setVisible(true);
    }

    private void actualizarResumen() {
        lblResumen.setText("Pendientes: " + gestor.getPendientes().size()
                + "    ·    Completadas: " + gestor.getCompletadas().size()
                + "    ·    Categorias: " + (arbol.contarCategorias(arbol.getRaiz()) - 1));
    }

    private void pendiente(String modulo) {
        JOptionPane.showMessageDialog(this, "El modulo \"" + modulo + "\" esta en desarrollo.",
                "En desarrollo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea salir de la aplicacion?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }
}