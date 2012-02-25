package aurelienribon.ui.components;

import aurelienribon.ui.components.TabPanelModel.TabModel;
import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.DeclarationSetProcessor;
import aurelienribon.ui.css.Property;
import aurelienribon.ui.css.DeclarationSet;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
class TabPanelHeaderPanel extends JLayeredPane {
	// -------------------------------------------------------------------------
	// Attributes + ctor
	// -------------------------------------------------------------------------

	private static final int MAX_HEIGHT = 20;
	private static final int MIN_HEIGHT = 1;
	private static final int OVERLAP = 1;
	private static final int SELECTED_LAYER = Integer.MAX_VALUE;

	private final List<TabPanelHeaderSubPanel> subPanels = new ArrayList<TabPanelHeaderSubPanel>();
	private final Callback callback;
	private int tabsLayout;
	private DeclarationSet styleAttrs;
	private Color stroke = Color.RED;

	public TabPanelHeaderPanel(int layout, Callback callback) {
		this.tabsLayout = layout;
		this.callback = callback;

		setOpaque(false);
		setHeight(MIN_HEIGHT);

		addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {
				reload();
			}
		});
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public interface Callback {
		public void selectTabRequested(TabModel tm);
		public void closeTabRequested(TabModel tm);
		public void closeAllTabsRequested();
		public void closeOtherTabsRequested(TabModel tm);
	}

	public void setTabsLayout(int tabsLayout) {
		this.tabsLayout = tabsLayout;
		reload();
	}

	public int getTabsLayout() {
		return tabsLayout;
	}

	public void addTab(TabModel tm) {
		TabPanelHeaderSubPanel subPanel = new TabPanelHeaderSubPanel(tm, new TabPanelHeaderSubPanel.Callback() {
			@Override public void selectRequested(TabModel tm) {callback.selectTabRequested(tm);}
			@Override public void closeRequested(TabModel tm) {callback.closeTabRequested(tm);reload();}
			@Override public void closeAllRequested() {callback.closeAllTabsRequested();reload();}
			@Override public void closeOthersRequested(TabModel tm) {callback.closeOtherTabsRequested(tm);reload();}
		});

		add(subPanel);
		subPanels.add(subPanel);
		reload();
		setHeight(MAX_HEIGHT);
		if (styleAttrs != null) Style.apply(subPanel, styleAttrs);
	}

	public void removeTab(TabModel model) {
		TabPanelHeaderSubPanel subPanel = findSubPanel(model);

		subPanels.remove(subPanel);
		remove(subPanel);
		if (subPanels.isEmpty()) setHeight(MIN_HEIGHT);
		revalidate();
		repaint();
	}

	public void removeAllTabs() {
		subPanels.clear();
		removeAll();
		setHeight(MIN_HEIGHT);
		revalidate();
		repaint();
	}

	public void reload() {
		int layout = tabsLayout;

		if (layout == TabPanel.LAYOUT_STACK) {
			int totalWidth = OVERLAP;
			for (int i=0; i<subPanels.size(); i++) totalWidth += subPanels.get(i).getPreferredSize().width - OVERLAP;
			if (totalWidth > getWidth()) layout = TabPanel.LAYOUT_GRID;
		}

		int width = 0;

		for (int i=0; i<subPanels.size(); i++) {
			TabPanelHeaderSubPanel subPanel = subPanels.get(i);
			setLayer(subPanel, subPanel.getModel().selected ? SELECTED_LAYER : i);

			switch (layout) {
				case TabPanel.LAYOUT_STACK:
					subPanel.setBounds(width, 0, subPanel.getPreferredSize().width, MAX_HEIGHT);
					subPanel.reload();
					width += subPanel.getPreferredSize().width - OVERLAP;
					break;

				case TabPanel.LAYOUT_GRID:
					int w = i < subPanels.size()-1 ? getWidth()/subPanels.size() : getWidth()-width;
					subPanel.setBounds(width, 0, w, MAX_HEIGHT);
					subPanel.reload();
					width += subPanel.getWidth() - OVERLAP;
					break;
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (stroke != null) {
			g.setColor(stroke);
			g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
		}
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void setHeight(int h) {
		setMinimumSize(new Dimension(20, h));
		setPreferredSize(new Dimension(20, h));
		setMaximumSize(new Dimension(20, h));
		revalidate();
	}

	private TabPanelHeaderSubPanel findSubPanel(TabModel model) {
		for (TabPanelHeaderSubPanel subPanel : subPanels) if (subPanel.getModel() == model) return subPanel;
		return null;
	}

	// -------------------------------------------------------------------------
	// StyleProcessor
	// -------------------------------------------------------------------------

	public static class Processor implements DeclarationSetProcessor<TabPanelHeaderPanel> {
		@Override
		public void process(TabPanelHeaderPanel target, DeclarationSet rs) {
			Property rule = AruiRules.STROKE;
			if (rs.contains(rule)) {target.stroke = (Color) rs.getValue(rule).get(0);}

			for (TabPanelHeaderSubPanel p : target.subPanels) Style.apply(p, rs);
			target.reload();
		}
	};
}
