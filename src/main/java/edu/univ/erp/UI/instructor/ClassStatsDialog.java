package edu.univ.erp.UI.instructor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.NormalDistributionFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassStatsDialog extends JDialog {

  private List<ArrayList<String>> gradebookData;
  private List<Double> totalScores;

  // Statistics variables
  private double mean;
  private double stdDev;
  private double meanQuiz;
  private double meanMidterm;
  private double meanFinal;

  public ClassStatsDialog(Frame owner, List<ArrayList<String>> gradebookData) throws Exception {
    super(owner, "Class Statistics & Relative Grading", true);
    this.gradebookData = gradebookData;
    this.totalScores = new ArrayList<>();

    setSize(1000, 700);
    setLocationRelativeTo(owner);
    setLayout(new BorderLayout(10, 10));

    // 1. Process Data
    calculateStats();

    // 2. UI Layout
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(createStatsPanel());
    splitPane.setRightComponent(createChartPanel());
    splitPane.setResizeWeight(0.3); // 30% for stats

    add(splitPane, BorderLayout.CENTER);
  }

  private void calculateStats() {
    if (gradebookData == null || gradebookData.isEmpty()) return;

    double sumQuiz = 0;
    double sumMid = 0;
    double sumFin = 0;
    int count = 0;

    // Parse scores from the gradebook list
    for (ArrayList<String> row : gradebookData) {
      try {
        // Calculate individual weighted components
        double q = computeComponentScore(row.get(2), row.get(3), row.get(4));
        double m = computeComponentScore(row.get(5), row.get(6), row.get(7));
        double f = computeComponentScore(row.get(8), row.get(9), row.get(10));

        double total = q + m + f;
        totalScores.add(total);

        // Accumulate sums for component averages
        sumQuiz += q;
        sumMid += m;
        sumFin += f;
        count++;

      } catch (Exception e) {
        // Skip rows with incomplete data
      }
    }

    if (totalScores.isEmpty()) return;

    // Calculate Averages
    meanQuiz = sumQuiz / count;
    meanMidterm = sumMid / count;
    meanFinal = sumFin / count;

    // Calculate Total Mean
    double sum = 0;
    for (Double s : totalScores) sum += s;
    mean = sum / totalScores.size();

    // Calculate Standard Deviation
    double sumSqDiff = 0;
    for (Double s : totalScores) {
      sumSqDiff += Math.pow(s - mean, 2);
    }
    stdDev = Math.sqrt(sumSqDiff / totalScores.size());
  }

  private double computeComponentScore(String s, String t, String w) {
    if (s == null || s.isEmpty() || t == null || t.isEmpty() || w == null || w.isEmpty()) return 0;
    try {
      double score = Double.parseDouble(s);
      double total = Double.parseDouble(t);
      double weight = Double.parseDouble(w);
      if (total == 0) return 0;
      return (score / total) * weight;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private JPanel createStatsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // --- Statistics ---
    JPanel avgPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    avgPanel.setBorder(new TitledBorder("Class Statistics"));

    avgPanel.add(new JLabel("Total Students:"));
    avgPanel.add(new JLabel(String.valueOf(totalScores.size())));

    // Component Averages
    avgPanel.add(new JLabel("Avg Quiz:"));
    avgPanel.add(new JLabel(String.format("%.2f", meanQuiz)));

    avgPanel.add(new JLabel("Avg Midterm:"));
    avgPanel.add(new JLabel(String.format("%.2f", meanMidterm)));

    avgPanel.add(new JLabel("Avg Final:"));
    avgPanel.add(new JLabel(String.format("%.2f", meanFinal)));

    // Separator space logic via labels for clarity
    avgPanel.add(new JLabel("----------------"));
    avgPanel.add(new JLabel("----------------"));

    avgPanel.add(new JLabel("Total Mean:"));
    avgPanel.add(new JLabel(String.format("%.2f", mean)));

    avgPanel.add(new JLabel("Std Deviation:"));
    avgPanel.add(new JLabel(String.format("%.2f", stdDev)));

    panel.add(avgPanel);
    panel.add(Box.createRigidArea(new Dimension(0, 20)));

    // --- Grading Slabs ---
    JPanel slabsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    slabsPanel.setBorder(new TitledBorder("Relative Grading Slabs"));

    addSlabRow(slabsPanel, "A+ (10)", Math.min(95, mean + (2.0 * stdDev)));
    addSlabRow(slabsPanel, "A  (10)", mean + (1.5 * stdDev));
    addSlabRow(slabsPanel, "A- (9)",  mean + (1.0 * stdDev));
    addSlabRow(slabsPanel, "B  (8)",  mean + (0.5 * stdDev));
    addSlabRow(slabsPanel, "B- (7)",  mean);
    addSlabRow(slabsPanel, "C  (6)",  mean - (0.5 * stdDev));
    addSlabRow(slabsPanel, "C- (5)",  mean - (1.0 * stdDev));
    addSlabRow(slabsPanel, "D  (4)",  mean - (1.5 * stdDev));

    slabsPanel.add(new JLabel("F  (0):"));
    slabsPanel.add(new JLabel("< " + String.format("%.2f", Double.max(mean - (1.5 * stdDev),30.0))));

    panel.add(slabsPanel);
    panel.add(Box.createVerticalGlue());

    return panel;
  }

  private void addSlabRow(JPanel p, String grade, double cutoff) {
    double displayCutoff = Math.max(0, Math.min(100, cutoff));
    p.add(new JLabel(grade + ":"));
    p.add(new JLabel(">= " + String.format("%.2f", displayCutoff)));
  }

  private JPanel createChartPanel() throws Exception {
    if (totalScores.isEmpty()) return new JPanel();

    double safeStdDev = (stdDev == 0) ? 0.1 : stdDev;
    NormalDistributionFunction2D normalDist = new NormalDistributionFunction2D(mean, safeStdDev);

    // Sample curve data
    XYDataset dataset = DatasetUtilities.sampleFunction2D(
        normalDist,
        Math.max(0, mean - 4 * safeStdDev),
        Math.min(100, mean + 4 * safeStdDev),
        100,
        "Normal Distribution"
    );

    JFreeChart chart = ChartFactory.createXYLineChart(
        "Grade Distribution (Bell Curve)",
        "Total Score",
        "Probability Density",
        dataset,
        PlotOrientation.VERTICAL,
        true, true, false
    );

    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesLinesVisible(0, true);
    renderer.setSeriesShapesVisible(0, false);
    plot.setRenderer(renderer);

    // --- Student Scores Dots ---
    XYSeries studentSeries = new XYSeries("Student Scores");
    for(Double score : totalScores) {
      double y = normalDist.getValue(score);
      studentSeries.add(score.doubleValue(), y);
    }

    XYSeriesCollection dotDataset = new XYSeriesCollection(studentSeries);
    plot.setDataset(1, dotDataset);
    XYLineAndShapeRenderer dotRenderer = new XYLineAndShapeRenderer(false, true);
    plot.setRenderer(1, dotRenderer);

    return new ChartPanel(chart);
  }
}