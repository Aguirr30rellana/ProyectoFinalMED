package vista.estilos;

import javax.swing.*;
import java.awt.*;

/*
 * Boton plano con esquinas redondeadas y efecto al pasar el mouse.
 * Uso:  BotonModerno.primario("Guardar"),  .peligro("Eliminar"),  .secundario("Cancelar")
 */
public class BotonModerno extends JButton {

    private final Color fondo;
    private final boolean contorno;     // true = fondo blanco con borde

    private BotonModerno(String texto, Color fondo, boolean contorno) {
        super(texto);
        this.fondo = fondo;
        this.contorno = contorno;
        setFont(Tema.fuente(Font.BOLD, 13));
        setForeground(contorno ? Tema.TEXTO : Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
    }

    public static BotonModerno primario(String texto)   { return new BotonModerno(texto, Tema.PRIMARIO, false); }
    public static BotonModerno peligro(String texto)    { return new BotonModerno(texto, Tema.PELIGRO, false); }
    public static BotonModerno secundario(String texto) { return new BotonModerno(texto, Tema.SUPERFICIE, true); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = fondo;
        if (getModel().isPressed()) {
            color = contorno ? Tema.BORDE : fondo.darker();
        } else if (getModel().isRollover()) {
            color = contorno ? Tema.FONDO : Tema.aclarar(fondo, 0.15);
        }

        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        if (contorno) {
            g2.setColor(Tema.BORDE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        }
        g2.dispose();
        super.paintComponent(g);   // dibuja el texto encima
    }
}