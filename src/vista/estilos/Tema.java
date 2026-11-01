package vista.estilos;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;

/*
 * Colores, fuentes y bordes de toda la aplicacion.
 */
public final class Tema {

    private Tema() { }

    // Paleta
    public static final Color PRIMARIO        = new Color(79, 70, 229);
    public static final Color PRIMARIO_OSCURO = new Color(55, 48, 163);
    public static final Color SECUNDARIO      = new Color(124, 58, 237);
    public static final Color PELIGRO         = new Color(220, 38, 38);
    public static final Color EXITO           = new Color(22, 163, 74);
    public static final Color FONDO           = new Color(243, 244, 246);
    public static final Color SUPERFICIE      = Color.WHITE;
    public static final Color BORDE           = new Color(226, 232, 240);
    public static final Color TEXTO           = new Color(17, 24, 39);
    public static final Color TEXTO_SUAVE     = new Color(107, 114, 128);
    public static final Color CONSOLA_FONDO   = new Color(30, 41, 59);
    public static final Color CONSOLA_TEXTO   = new Color(226, 232, 240);

    // Colores de acento para tarjetas y niveles del arbol
    public static final Color[] ACENTOS = {
            new Color(79, 70, 229),    // indigo
            new Color(16, 185, 129),   // verde
            new Color(245, 158, 11),   // ambar
            new Color(236, 72, 153),   // rosa
            new Color(14, 165, 233),   // celeste
            new Color(139, 92, 246)    // violeta
    };

    private static final String FAMILIA = elegirFuente("Segoe UI", "Inter", "Roboto", "SansSerif");
    private static final String FAMILIA_MONO = elegirFuente("Consolas", "JetBrains Mono", "Monospaced");

    private static String elegirFuente(String... opciones) {
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String opcion : opciones) {
            if (Arrays.asList(disponibles).contains(opcion)) return opcion;
        }
        return opciones[opciones.length - 1];
    }

    public static Font fuente(int estilo, int tamano) { return new Font(FAMILIA, estilo, tamano); }
    public static Font fuenteMono(int tamano)         { return new Font(FAMILIA_MONO, Font.PLAIN, tamano); }

    // Mezcla un color con blanco (0.0 = igual, 1.0 = blanco). Sirve para fondos suaves.
    public static Color aclarar(Color c, double cantidad) {
        int r = (int) (c.getRed()   + (255 - c.getRed())   * cantidad);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * cantidad);
        int b = (int) (c.getBlue()  + (255 - c.getBlue())  * cantidad);
        return new Color(r, g, b);
    }

    // Panel blanco con borde fino: la "tarjeta" base de la interfaz
    public static JPanel tarjeta(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        if (titulo != null) {
            JLabel lbl = new JLabel(titulo.toUpperCase());
            lbl.setFont(fuente(Font.BOLD, 11));
            lbl.setForeground(TEXTO_SUAVE);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            panel.add(lbl, BorderLayout.NORTH);
        }
        return panel;
    }

    public static Border margen(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    // Look & Feel del sistema + fuentes del tema en los dialogos
    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla se queda el aspecto por defecto
        }
        UIManager.put("OptionPane.messageFont", fuente(Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", fuente(Font.PLAIN, 13));
        UIManager.put("Label.font", fuente(Font.PLAIN, 13));
        UIManager.put("TextField.font", fuente(Font.PLAIN, 13));
        UIManager.put("Spinner.font", fuente(Font.PLAIN, 13));
    }
}