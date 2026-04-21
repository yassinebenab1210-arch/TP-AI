import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Tunisia Route Finder — redesigned UI.
 *
 * <p>Features:
 * <ul>
 *   <li>Full dark-mode map canvas with grid overlay and animated route drawing</li>
 *   <li>Click-to-select cities directly on the map</li>
 *   <li>Side panel with glassy stats cards and algorithm selector</li>
 *   <li>Smooth path animation with trail pulse effect</li>
 *   <li>Hover tooltips showing city name + coordinates</li>
 * </ul>
 */
public class TunisiaMapApp extends JFrame {

    // ============================= THEME =====================================
    private static final Color C_BG           = new Color(10, 12, 18);
    private static final Color C_SURFACE      = new Color(18, 22, 32);
    private static final Color C_BORDER       = new Color(35, 42, 60);
    private static final Color C_ACCENT       = new Color(0, 210, 180);     // teal
    private static final Color C_ACCENT2      = new Color(255, 160, 50);    // amber
    private static final Color C_TEXT_HI      = new Color(230, 235, 245);
    private static final Color C_TEXT_MED     = new Color(140, 150, 170);
    private static final Color C_TEXT_DIM     = new Color(70, 80, 100);
    private static final Color C_NODE_DEFAULT = new Color(60, 75, 110);
    private static final Color C_NODE_ON_PATH = new Color(0, 210, 180);
    private static final Color C_NODE_START   = new Color(80, 220, 120);
    private static final Color C_NODE_GOAL    = new Color(255, 90, 90);
    private static final Color C_EDGE_DEFAULT = new Color(30, 38, 55);
    private static final Color C_EDGE_PATH    = new Color(0, 210, 180);
    private static final Color C_OCEAN        = new Color(8, 14, 28);

    // ============================= GEO BOUNDS ================================
    // Tunisia bounding box
    private static final double LAT_MIN = 30.2, LAT_MAX = 37.6;
    private static final double LON_MIN =  7.5, LON_MAX = 11.6;

    // ============================= ANIMATION =================================
    private static final int   ANIM_STEPS     = 40;
    private static final int   ANIM_DELAY_MS  = 18;
    private int                animStep       = ANIM_STEPS; // starts complete
    private Timer              animTimer;

    // ============================= STATE =====================================
    private final Map<String, CityData> cityMap = new LinkedHashMap<>();
    private final Graph                 graph   = new Graph();

    private String       startCity    = null;
    private String       goalCity     = null;
    private SearchResult currentResult = null;

    private String       hoveredCity  = null;
    private String       selectedAlgo = "Best-First";

    private MapCanvas    canvas;
    private StatsPanel   statsPanel;
    private JLabel       statusLabel;

    // ============================= CONSTRUCTOR ===============================

    public TunisiaMapApp() {
        super("Tunisia Route Finder");
        loadData();
        buildUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1440, 880);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setBackground(C_BG);
    }

    // ============================= DATA ======================================

    private void loadData() {
        try {
            DataLoader.loadAll(cityMap, graph);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Failed to load map data:\n" + ex.getMessage(),
                "Data Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // ============================= UI ASSEMBLY ================================

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);
        setContentPane(root);

        canvas = new MapCanvas();
        root.add(canvas, BorderLayout.CENTER);

        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        JPanel statusBar = buildStatusBar();
        root.add(statusBar, BorderLayout.SOUTH);
    }

    // ============================= SIDEBAR ===================================

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(C_SURFACE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, C_BORDER));
        sidebar.setPreferredSize(new Dimension(280, 0));

        statsPanel = new StatsPanel(); // must exist before buildCityPicker triggers runSearch

        sidebar.add(buildBrand());
        sidebar.add(buildDivider());
        sidebar.add(buildAlgoSelector());
        sidebar.add(buildDivider());
        sidebar.add(buildCityPicker());
        sidebar.add(buildDivider());
        sidebar.add(statsPanel);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildLegend());

        return sidebar;
    }

    private JPanel buildBrand() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(new EmptyBorder(22, 20, 18, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel icon = new JLabel("◈  ROUTE FINDER");
        icon.setForeground(C_ACCENT);
        icon.setFont(new Font("Monospaced", Font.BOLD, 13));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Tunisia Road Network");
        sub.setForeground(C_TEXT_MED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(icon);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JPanel buildAlgoSelector() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel title = sectionLabel("ALGORITHM");
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        String[] algos = {"Best-First", "UCS", "BFS", "DFS"};
        ButtonGroup group = new ButtonGroup();
        for (String algo : algos) {
            AlgoRadio rb = new AlgoRadio(algo);
            rb.setSelected(algo.equals(selectedAlgo));
            rb.addActionListener(e -> { selectedAlgo = algo; runSearch(); });
            group.add(rb);
            p.add(rb);
            p.add(Box.createVerticalStrut(5));
        }
        return p;
    }

    private JPanel buildCityPicker() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        p.add(sectionLabel("CITIES"));
        p.add(Box.createVerticalStrut(10));

        String[] cities = cityMap.keySet().stream()
            .map(s -> s.replace('_', ' ')).sorted().toArray(String[]::new);

        JComboBox<String> startCombo = buildCombo(cities);
        JComboBox<String> goalCombo  = buildCombo(cities);

        // default selection
        setComboItem(startCombo, "Tunis");
        setComboItem(goalCombo,  "Tozeur");

        ActionListener refresh = e -> {
            startCity = ((String) startCombo.getSelectedItem()).replace(' ', '_');
            goalCity  = ((String) goalCombo.getSelectedItem()).replace(' ', '_');
            runSearch();
        };
        startCombo.addActionListener(refresh);
        goalCombo.addActionListener(refresh);

        p.add(fieldLabel("From"));
        p.add(Box.createVerticalStrut(4));
        p.add(startCombo);
        p.add(Box.createVerticalStrut(10));
        p.add(fieldLabel("To"));
        p.add(Box.createVerticalStrut(4));
        p.add(goalCombo);

        // trigger initial search
        SwingUtilities.invokeLater(() -> {
            startCity = "Tunis";
            goalCity  = "Tozeur";
            runSearch();
        });

        return p;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(new EmptyBorder(14, 20, 20, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        p.add(sectionLabel("LEGEND"));
        p.add(Box.createVerticalStrut(10));

        p.add(legendRow(C_NODE_START,   "Start city"));
        p.add(Box.createVerticalStrut(5));
        p.add(legendRow(C_NODE_GOAL,    "Goal city"));
        p.add(Box.createVerticalStrut(5));
        p.add(legendRow(C_NODE_ON_PATH, "Path waypoint"));
        p.add(Box.createVerticalStrut(5));
        p.add(legendRow(C_NODE_DEFAULT, "Other city"));
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 5));
        bar.setBackground(new Color(14, 17, 26));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        bar.setPreferredSize(new Dimension(0, 30));

        statusLabel = new JLabel("Click two cities on the map — or use the dropdowns.");
        statusLabel.setForeground(C_TEXT_MED);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        bar.add(statusLabel);
        return bar;
    }

    // ============================= SEARCH ====================================

    private void runSearch() {
        if (startCity == null || goalCity == null) return;

        SearchResult result = switch (selectedAlgo) {
            case "BFS"        -> BFS.run(graph, startCity, goalCity);
            case "DFS"        -> DFS.run(graph, startCity, goalCity);
            case "UCS"        -> UCS.run(graph, startCity, goalCity);
            default           -> BestFirst.run(graph, cityMap, startCity, goalCity);
        };

        currentResult = result;
        statsPanel.update(result, selectedAlgo);

        if (result != null) {
            statusLabel.setText("Path found: " + String.join(" → ", result.path));
        } else {
            statusLabel.setText("No path found between " + startCity + " and " + goalCity + ".");
        }

        animateRoute();
    }

    private void animateRoute() {
        if (animTimer != null) animTimer.cancel();
        animStep = 0;
        animTimer = new Timer(true);
        animTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                if (animStep < ANIM_STEPS) {
                    animStep++;
                    canvas.repaint();
                } else {
                    cancel();
                }
            }
        }, 0, ANIM_DELAY_MS);
    }

    // ============================= MAP CANVAS ================================

    private class MapCanvas extends JPanel {
        private Point mousePos = new Point();

        MapCanvas() {
            setBackground(C_OCEAN);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { handleMapClick(e.getX(), e.getY()); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                    updateHover(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            recalcPixelPositions();

            drawOceanBackground(g2);
            drawTunisiaShape(g2);
            drawGrid(g2);
            drawAllEdges(g2);
            drawPathEdges(g2);
            drawCities(g2);
            drawTooltip(g2);

            g2.dispose();
        }

        // ---- coordinate mapping ----

        private int lonToX(double lon) {
            // Tunisia spans ~4.1° lon and ~7.4° lat
            // At lat ~34°, 1° lon ≈ 0.829 * 1° lat in distance
            // So we scale lon range to match the visual aspect ratio
            double lonRange = LON_MAX - LON_MIN;
            double latRange = LAT_MAX - LAT_MIN;
            double aspectCorrection = (lonRange / latRange) * (Math.cos(Math.toRadians(33.8)));
            int availH = getHeight() - 60;
            int availW = (int) (availH * aspectCorrection);
            int marginX = (getWidth() - availW) / 2;
            return (int) ((lon - LON_MIN) / lonRange * availW + marginX);
        }
        private int latToY(double lat) {
            double latRange = LAT_MAX - LAT_MIN;
            double lonRange = LON_MAX - LON_MIN;
            double aspectCorrection = (lonRange / latRange) * (Math.cos(Math.toRadians(33.8)));
            int availH = getHeight() - 60;
            int availW = (int) (availH * aspectCorrection);
            int marginX = (getWidth() - availW) / 2;
            // keep vertical centered too
            return (int) ((LAT_MAX - lat) / latRange * availH + 30);
        }

        private void recalcPixelPositions() {
            for (CityData cd : cityMap.values()) {
                cd.px = lonToX(cd.lon);
                cd.py = latToY(cd.lat);
            }
        }

        // ---- drawing routines ----

        private void drawOceanBackground(Graphics2D g) {
            g.setColor(C_OCEAN);
            g.fillRect(0, 0, getWidth(), getHeight());
            // subtle vignette
            RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Double(getWidth() / 2.0, getHeight() / 2.0),
                (float) Math.max(getWidth(), getHeight()) * 0.7f,
                new float[]{0f, 1f},
                new Color[]{C_OCEAN, new Color(0, 0, 0, 160)}
            );
            g.setPaint(vignette);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        /**
         * Draws an approximate outline of Tunisia as a filled land polygon.
         * Coordinates are real border waypoints in (lon, lat) order.
         */
        private void drawTunisiaShape(Graphics2D g) {
            // Accurate Tunisia border clockwise from NW corner
            double[][] border = {
                // Northwestern tip
                {8.58, 37.13}, {8.63, 37.18}, {8.75, 37.27}, {9.00, 37.24},
                {9.20, 37.35}, {9.48, 37.23}, {9.70, 37.34},
                // North coast east toward Cape Bon
                {9.88, 37.18}, {10.18, 37.05}, {10.40, 36.97},
                // Cape Bon peninsula - distinctive NE protrusion
                {10.59, 36.90}, {10.78, 36.80}, {11.03, 36.87},
                {11.13, 36.72}, {11.08, 36.58}, {10.92, 36.51},
                {10.73, 36.43}, {10.60, 36.41},
                // East coast going south
                {10.70, 36.10}, {10.85, 35.85}, {11.05, 35.60},
                {11.12, 35.30}, {11.10, 35.00}, {10.95, 34.70},
                {10.82, 34.42},
                // Gulf of Gabès - large indent westward
                {10.88, 34.10}, {11.10, 33.80}, {11.18, 33.52},
                {11.51, 33.19}, {11.57, 32.81},
                // Jerba area, then southeast coast
                {11.53, 32.48}, {11.40, 32.20}, {11.20, 31.95},
                {11.05, 31.77}, {10.75, 31.50},
                // Border with Libya going southwest
                {10.50, 31.20}, {10.20, 31.00}, {10.00, 30.83},
                // Southern border (Libya→Algeria), west
                {9.52, 30.23}, {9.20, 30.12}, {8.90, 30.10},
                {8.60, 30.19}, {8.20, 30.35},
                // Algerian border going north (mostly straight)
                {7.49, 30.48}, {7.50, 31.00}, {7.50, 31.50},
                {7.52, 32.10}, {7.60, 32.50},
                {7.65, 32.80}, {7.60, 33.10}, {7.55, 33.50},
                {7.49, 33.87}, {7.50, 34.20}, {7.50, 34.65},
                {7.52, 35.00}, {7.55, 35.30},
                // Northwest — back to start
                {8.05, 35.52}, {8.20, 35.85}, {8.28, 36.20},
                {8.38, 36.47}, {8.47, 36.77}, {8.55, 36.97},
                {8.58, 37.13}
            };

            int n = border.length;
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = lonToX(border[i][0]);
                ys[i] = latToY(border[i][1]);
            }

            // filled land
            g.setColor(new Color(38, 58, 38, 180));
            g.fillPolygon(xs, ys, n);

            // subtle inner highlight
            g.setColor(new Color(55, 80, 50, 60));
            g.setStroke(new BasicStroke(6f));
            g.drawPolygon(xs, ys, n);

            // crisp border
            g.setColor(new Color(80, 110, 70, 200));
            g.setStroke(new BasicStroke(1.5f));
            g.drawPolygon(xs, ys, n);
        }

        private void drawGrid(Graphics2D g) {
            g.setColor(new Color(25, 35, 55, 90));
            g.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{4f, 8f}, 0f));
            for (double lat = 31; lat < LAT_MAX; lat++) {
                int y = latToY(lat);
                g.drawLine(0, y, getWidth(), y);
            }
            for (double lon = 8; lon < LON_MAX; lon++) {
                int x = lonToX(lon);
                g.drawLine(x, 0, x, getHeight());
            }
        }

        private void drawAllEdges(Graphics2D g) {
            g.setColor(C_EDGE_DEFAULT);
            g.setStroke(new BasicStroke(0.8f));
            for (Map.Entry<String, List<Edge>> entry : graph.adj.entrySet()) {
                CityData from = cityMap.get(entry.getKey());
                if (from == null) continue;
                for (Edge edge : entry.getValue()) {
                    CityData to = cityMap.get(edge.dest);
                    if (to == null) continue;
                    if (entry.getKey().compareTo(edge.dest) < 0) {
                        g.drawLine(from.px, from.py, to.px, to.py);
                    }
                }
            }
        }

        private void drawPathEdges(Graphics2D g) {
            if (currentResult == null || currentResult.path.size() < 2) return;

            List<String> path = currentResult.path;
            double progress = (double) animStep / ANIM_STEPS;
            double totalSegs = path.size() - 1;

            for (int i = 0; i < totalSegs; i++) {
                double segStart = i       / totalSegs;
                double segEnd   = (i + 1) / totalSegs;

                if (progress < segStart) break;

                CityData from = cityMap.get(path.get(i));
                CityData to   = cityMap.get(path.get(i + 1));
                if (from == null || to == null) continue;

                double segProgress = Math.min(1.0, (progress - segStart) / (segEnd - segStart));
                int tx = (int) (from.px + (to.px - from.px) * segProgress);
                int ty = (int) (from.py + (to.py - from.py) * segProgress);

                // glowing path
                drawGlowLine(g, from.px, from.py, tx, ty, C_EDGE_PATH, 4f);
            }
        }

        private void drawGlowLine(Graphics2D g, int x1, int y1, int x2, int y2,
                                   Color base, float width) {
            // outer glow
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 40));
            g.setStroke(new BasicStroke(width + 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
            // mid
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 90));
            g.setStroke(new BasicStroke(width + 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
            // core
            g.setColor(base);
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
        }

        private void drawCities(Graphics2D g) {
            Set<String> pathSet = pathCitySet();

            for (CityData cd : cityMap.values()) {
                boolean isStart  = cd.id.equals(startCity);
                boolean isGoal   = cd.id.equals(goalCity);
                boolean onPath   = pathSet.contains(cd.id);
                boolean hovered  = cd.id.equals(hoveredCity);

                Color nodeColor = isStart ? C_NODE_START
                                : isGoal  ? C_NODE_GOAL
                                : onPath  ? C_NODE_ON_PATH
                                :           C_NODE_DEFAULT;

                int r = isStart || isGoal ? 7 : onPath ? 5 : 4;

                // halo
                if (isStart || isGoal || hovered) {
                    g.setColor(new Color(nodeColor.getRed(), nodeColor.getGreen(),
                                         nodeColor.getBlue(), 50));
                    g.fillOval(cd.px - r - 5, cd.py - r - 5, (r + 5) * 2, (r + 5) * 2);
                }

                // fill
                g.setColor(nodeColor);
                g.fillOval(cd.px - r, cd.py - r, r * 2, r * 2);

                // rim
                g.setColor(isStart || isGoal ? nodeColor.brighter() : C_SURFACE);
                g.setStroke(new BasicStroke(1.2f));
                g.drawOval(cd.px - r, cd.py - r, r * 2, r * 2);

                // label (show for path cities and hovered)
                if (onPath || isStart || isGoal || hovered) {
                    drawCityLabel(g, cd, nodeColor, isStart || isGoal);
                }
            }
        }

        private void drawCityLabel(Graphics2D g, CityData cd, Color color, boolean bold) {
            String label = cd.display;
            Font font = bold
                ? new Font("SansSerif", Font.BOLD, 11)
                : new Font("SansSerif", Font.PLAIN, 10);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int lw = fm.stringWidth(label);

            // bg pill
            g.setColor(new Color(10, 12, 18, 190));
            g.fillRoundRect(cd.px + 8 - 2, cd.py - 9, lw + 6, 14, 4, 4);

            g.setColor(color);
            g.drawString(label, cd.px + 9, cd.py + 1);
        }

        private void drawTooltip(Graphics2D g) {
            if (hoveredCity == null) return;
            CityData cd = cityMap.get(hoveredCity);
            if (cd == null) return;

            String[] lines = {
                cd.display,
                String.format("%.4f°N  %.4f°E", cd.lat, cd.lon)
            };
            Font f0 = new Font("SansSerif", Font.BOLD, 12);
            Font f1 = new Font("Monospaced", Font.PLAIN, 10);
            g.setFont(f0);
            int w = Math.max(g.getFontMetrics().stringWidth(lines[0]) + 16,
                             g.getFontMetrics(f1).stringWidth(lines[1]) + 16);
            int h = 40;
            int tx = mousePos.x + 14, ty = mousePos.y - 44;
            if (tx + w > getWidth()  - 8) tx = mousePos.x - w - 14;
            if (ty < 8)                   ty = mousePos.y + 14;

            g.setColor(new Color(18, 22, 36, 230));
            g.fillRoundRect(tx, ty, w, h, 8, 8);
            g.setColor(C_BORDER);
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(tx, ty, w, h, 8, 8);

            g.setFont(f0);
            g.setColor(C_TEXT_HI);
            g.drawString(lines[0], tx + 8, ty + 15);
            g.setFont(f1);
            g.setColor(C_TEXT_MED);
            g.drawString(lines[1], tx + 8, ty + 30);
        }

        // ---- interaction ----

        private void handleMapClick(int mx, int my) {
            String city = nearestCity(mx, my, 18);
            if (city == null) return;

            if (startCity == null || (startCity != null && goalCity != null)) {
                startCity = city;
                goalCity  = null;
                currentResult = null;
                statsPanel.clear();
                statusLabel.setText("Start: " + startCity.replace('_',' ') + " — now click a goal city.");
            } else {
                if (city.equals(startCity)) return;
                goalCity = city;
                syncCombosFromState();
                runSearch();
            }
            canvas.repaint();
        }

        private void updateHover(int mx, int my) {
            String prev  = hoveredCity;
            hoveredCity = nearestCity(mx, my, 14);
            if (!Objects.equals(prev, hoveredCity)) repaint();
        }

        private String nearestCity(int mx, int my, int threshold) {
            String best = null;
            int    bestD = threshold * threshold;
            for (CityData cd : cityMap.values()) {
                int dx = cd.px - mx, dy = cd.py - my;
                int d2 = dx * dx + dy * dy;
                if (d2 < bestD) { bestD = d2; best = cd.id; }
            }
            return best;
        }
    }

    // ============================= STATS PANEL ===============================

    private class StatsPanel extends JPanel {
        private JLabel[] labels = new JLabel[4];
        private JLabel   algoLabel;
        private String[] keys   = {"Distance", "Nodes explored", "Path length", "Time"};

        StatsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(C_SURFACE);
            setBorder(new EmptyBorder(14, 20, 14, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            add(sectionLabel("RESULT"));
            add(Box.createVerticalStrut(10));

            algoLabel = new JLabel("—");
            algoLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
            algoLabel.setForeground(C_ACCENT2);
            algoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(algoLabel);
            add(Box.createVerticalStrut(8));

            for (int i = 0; i < 4; i++) {
                labels[i] = new JLabel("—");
                labels[i].setForeground(C_ACCENT);
                labels[i].setFont(new Font("Monospaced", Font.BOLD, 11));
                add(buildStatRow(keys[i], labels[i]));
                add(Box.createVerticalStrut(4));
            }
        }

        void update(SearchResult r, String algo) {
            algoLabel.setText(algo);
            if (r == null) { clear(); return; }
            labels[0].setText(r.totalCost + " km");
            labels[1].setText(String.valueOf(r.nodesExplored));
            labels[2].setText(r.path.size() + " cities");
            labels[3].setText(r.timeMs + " ms");
            repaint();
        }

        void clear() {
            for (JLabel l : labels) l.setText("—");
            algoLabel.setText("—");
        }

        private JPanel buildStatRow(String key, JLabel valLabel) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(new Color(24, 29, 44));
            row.setBorder(new EmptyBorder(5, 8, 5, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel keyLabel = new JLabel(key);
            keyLabel.setForeground(C_TEXT_DIM);
            keyLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JPanel valWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            valWrapper.setBackground(new Color(24, 29, 44));
            valWrapper.add(valLabel);

            row.add(keyLabel, BorderLayout.WEST);
            row.add(valWrapper, BorderLayout.EAST);

            JPanel wrap = new JPanel();
            wrap.setLayout(new BoxLayout(wrap, BoxLayout.X_AXIS));
            wrap.setBackground(C_SURFACE);
            wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            wrap.add(row);
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            return wrap;
        }
    }

    // ============================= CUSTOM COMPONENTS =========================

    private static class AlgoRadio extends JRadioButton {
        AlgoRadio(String text) {
            super(text);
            setForeground(C_TEXT_MED);
            setBackground(C_SURFACE);
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setFocusPainted(false);
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addChangeListener(e -> {
                setForeground(isSelected() ? C_ACCENT : C_TEXT_MED);
                setFont(new Font("SansSerif", isSelected() ? Font.BOLD : Font.PLAIN, 12));
            });
        }
    }

    // ============================= HELPERS ===================================

    private JComboBox<String> buildCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(new Color(22, 27, 42));
        cb.setForeground(C_TEXT_HI);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(C_BORDER));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setOpaque(true);

        // Custom renderer so text is always visible regardless of L&F
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
                if (isSelected) {
                    lbl.setBackground(new Color(0, 150, 130));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(new Color(22, 27, 42));
                    lbl.setForeground(C_TEXT_HI);
                }
                lbl.setBorder(new EmptyBorder(3, 6, 3, 6));
                return lbl;
            }
        });

        return cb;
    }

    private static void setComboItem(JComboBox<String> cb, String value) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).replace(' ', '_').equalsIgnoreCase(value)) {
                cb.setSelectedIndex(i); return;
            }
        }
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(C_TEXT_DIM);
        l.setFont(new Font("Monospaced", Font.BOLD, 10));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(C_TEXT_MED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JSeparator buildDivider() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(C_BORDER);
        sep.setBackground(C_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private static JPanel legendRow(Color color, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(C_SURFACE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setForeground(C_TEXT_MED);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

        row.add(dot);
        row.add(lbl);
        return row;
    }

    private Set<String> pathCitySet() {
        if (currentResult == null) return Collections.emptySet();
        return new HashSet<>(currentResult.path);
    }

    private void syncCombosFromState() {
        // dropdowns are driven by action listeners; map clicks go direct
        // we just rely on canvas repaint + stats update
    }

    // ============================= MAIN ======================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new TunisiaMapApp().setVisible(true));
    }
}