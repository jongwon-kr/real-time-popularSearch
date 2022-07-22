package popularSearch;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HotKeywords extends JFrame implements ActionListener, MouseListener, ListSelectionListener {
	Desktop desktop;
	static URL url = null;
	static String naverNews = "https://news.naver.com/main/ranking/popularDay.naver";
	static String naverNews2 = "https://news.naver.com/main/ranking/popularMemo.naver";
	static String naverEntertainNews; // "https://entertain.naver.com/now#sid=106&date=" + todayDate + "&page=page
										// today는 오늘
	// 날짜, page는 페이지
	static BufferedReader reader = null;
	static int cntArticle = 0;
	static String[] articles;
	static HashMap<String, Integer> wordsMap;
	static String todayDate, keyword;

	static JComboBox JcomB;
	static JLabel date;
	static JButton updateBtn;
	static JPanel top_panel;
	static JPanel center_panel;
	static JPanel search_panel;
	static JList receiveList;
	static DefaultListModel receiveListModel;
	static JScrollPane resultPane; // 검색 결과

	public HotKeywords() {
		super("HotKeywords");
		receiveListModel = new DefaultListModel();
		receiveList = new JList(receiveListModel);
		receiveList.setBackground(new Color(0xFCFCFC));
		receiveList.setFont(new Font("Dialog", Font.BOLD, 18));
		receiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 리스트 목록중 하나만 선택
		receiveList.addListSelectionListener(this); // list선택 바뀜
		receiveList.addMouseListener(this); // 더블클릭

		top_panel = new JPanel(new GridLayout(2, 1));
		top_panel.setBackground(new Color(0x1DDB16));
		center_panel = new JPanel();
		center_panel.setBackground(new Color(0x1DDB16));
		search_panel = new JPanel(new FlowLayout());
		search_panel.setBackground(new Color(0x1DDB16));

		date = new JLabel("Date : " + getTime());
		date.setHorizontalAlignment(JLabel.CENTER);
		date.setFont(new Font("Dialog", Font.BOLD, 15));
		date.setForeground(Color.white);
		date.setPreferredSize(new Dimension(300, 20));
		top_panel.add("North", date);

		String[] topics = { "인기뉴스", "연예뉴스" };
		JcomB = new JComboBox(topics);
		JcomB.setBackground(Color.white);
		JcomB.setFont(new Font("Dialog", Font.BOLD, 12));
		JcomB.setFocusable(false);
		JcomB.setPreferredSize(new Dimension(120, 30));
		search_panel.add(JcomB);
		updateBtn = new JButton("검색");
		updateBtn.setBackground(Color.white);
		updateBtn.setFocusable(false);
		updateBtn.setFont(new Font("Dialog", Font.BOLD, 12));
		updateBtn.setPreferredSize(new Dimension(70, 30));
		updateBtn.addActionListener(this);
		updateBtn.setActionCommand("search");
		search_panel.add(updateBtn);
		top_panel.add("Center", search_panel);

		resultPane = new JScrollPane(receiveList);
		resultPane.setPreferredSize(new Dimension(270, 270));
		center_panel.add(resultPane);

		add("North", top_panel);
		add("Center", center_panel);
		setSize(300, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void search() {
		receiveListModel.removeAllElements();
		articles = getArticles();
		ArrayList<String> articlesList = new ArrayList<String>(Arrays.asList(articles));
		while (articlesList.remove(null)) {

		}
		String[] result = new String[articlesList.size()];
		int i = 0;
		for (String temp : articlesList) {
			result[i++] = temp;
		}
		checkWords(result);
	}

	// 기사 받아오기
	static String[] getArticles() {
		int cntPage = 1;
		boolean checkArticle = false;
		String[] getArticles;
		getArticles = new String[1000]; // 기사들
		String line = "", result, selectNews;
		Pattern p = Pattern.compile("[가-힣' '0-9'\'''\"'.]");
		String[] exceptWords = { "[가-힣]+러운", "[가-힣]+네", "[가-힣]+이", "[가-힣]+였", "[가-힣]+다", "[가-힣]+의", "[가-힣]+보다",
				"[가-힣]+와", "[가-힣]+은", "[가-힣]+지네", "[가-힣]+하네", "[0-9]+월", "[0-9]+대", "[0-9]+회", "[0-9]+번", "[0-9]+년",
				"[가-힣]+부터", "[가-힣]+까지", "[가-힣]+때문", "[가-힣]+니다", "없+[가-힣]", "[가-힣]+는", "[가-힣]+한", "[가-힣]+해야", "[가-힣]+려야",
				"[가-힣]+되야", "[가-힣]+요", "[가-힣]+에", "[가-힣]+에서", "정부", "대통령", "논란", "공무원", "정말", "뮤비", "이틀", "하루", "큰",
				"진짜", "배우", "가수", "앵콜", "이유", "활동", "포즈", "단독", "역할", "출연", "심해", "써라", "의혹", "하기", "노출", "어려", "국내",
				"개월", "해외", "라인업", "겨울", "가을", "사적", "여름", "봄", "때문", "중단", "새로운", "작품", "준비", "컴백", "파격", "복귀", "이후",
				"포토", "지지율", "결혼", "질문", "말씀", "너무", "결혼", "공개", "영상", "머리", "고백", "신곡", "입담", "영상", "당황", "채용", "그래",
				"그저", "하차", "입장", "발표", "브랜드", "같은", "다른", "사진", "사상", "출마", "후보", "신청", "서류", "대표", "속보", "-", "[.]",
				"\"", "'", "   ", "  " };
		try {
			selectNews = (String) JcomB.getSelectedItem();
			if (selectNews.equals("인기뉴스")) {
				url = new URL(naverNews);
				reader = new BufferedReader(new InputStreamReader(url.openStream(), "EUC-KR"));
				while ((line = reader.readLine()) != null) {
					result = "";
					if (line.contains("class=\"list_title nclicks('RBP.rnknws')")) {
						line = line.split("RBP.rnknws")[1].substring(4);
						line = line.substring(0, line.length() - 4);
						Matcher m = p.matcher(line);
						{
							while (m.find()) {
								result += m.group();
							}
						}
						for (int i = 0; i < exceptWords.length; i++) {
							if (result.contains(exceptWords[i])) {
								if (exceptWords[i].equals("-") || exceptWords[i].equals("[.]")
										|| exceptWords[i].equals("  ") || exceptWords[i].equals("   ")
										|| exceptWords[i].equals("[") || exceptWords[i].equals("]")
										|| exceptWords[i].equals("(") || exceptWords[i].equals(")")
										|| exceptWords[i].equals("{") || exceptWords[i].equals("}"))
									result = result.replaceAll(exceptWords[i], " ");
								else
									result = result.replaceAll(exceptWords[i], "");
							}
						}
						if (result.length() > 1) {
							getArticles[cntArticle++] = result;
						}

					}
				}
				url = new URL(naverNews2);
				reader = new BufferedReader(new InputStreamReader(url.openStream(), "EUC-KR"));
				while ((line = reader.readLine()) != null) {
					result = "";
					if (line.contains("class=\"list_title nclicks('RBP.cmtnws')")) {
						line = line.split("RBP.cmtnws")[1].substring(4);
						line = line.substring(0, line.length() - 4);
						Matcher m = p.matcher(line);
						{
							while (m.find()) {
								result += m.group();
							}
						}
						for (int i = 0; i < exceptWords.length; i++) {
							if (result.contains(exceptWords[i])) {
								if (exceptWords[i].equals("-") || exceptWords[i].equals("[.]")
										|| exceptWords[i].equals("  ") || exceptWords[i].equals("   ")
										|| exceptWords[i].equals("[") || exceptWords[i].equals("]")
										|| exceptWords[i].equals("(") || exceptWords[i].equals(")")
										|| exceptWords[i].equals("{") || exceptWords[i].equals("}"))
									result = result.replaceAll(exceptWords[i], " ");
								else
									result = result.replaceAll(exceptWords[i], "");
							}
						}
						if (result.length() > 1)
							getArticles[cntArticle++] = result;
					}
				}
			} else if (selectNews.equals("연예뉴스")) {
				checkArticle = true;
				while (checkArticle) {
					naverEntertainNews = "https://entertain.naver.com/now#sid=106&date=";
					naverEntertainNews = naverEntertainNews + getTime() + "&page=" + String.valueOf(cntPage++);
					url = new URL(naverEntertainNews);
					reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
					while ((line = reader.readLine()) != null) {
						if (cntPage == 100) {
							checkArticle = false;
						}
						result = "";
						if (line.contains("onclick=\"nclk(this, 'now.alist', '', '');\">")) {
							Matcher m = p.matcher(line);
							{
								while (m.find()) {
									result += m.group();
								}
							}
							for (int i = 0; i < exceptWords.length; i++) {
								if (exceptWords[i].equals("[-]") || exceptWords[i].equals("[.]")
										|| exceptWords[i].equals("  ") || exceptWords[i].equals("   ")
										|| exceptWords[i].equals("[") || exceptWords[i].equals("]")
										|| exceptWords[i].equals("(") || exceptWords[i].equals(")")
										|| exceptWords[i].equals("{") || exceptWords[i].equals("}"))
									result = result.replaceAll(exceptWords[i], " ");
								else
									result = result.replaceAll(exceptWords[i], "");
							}
							if (result.length() > 1) {
								result = result.split("  ")[1];

								getArticles[cntArticle++] = result;
							}
						}
					}
				}
			} else {
				System.out.println("error");
			}

		} catch (Exception e) {

		}
		return getArticles;
	}

	// 인기 단어 체크
	public static String[] checkWords(String[] strs) {
		String[] popularWords = new String[10];
		wordsMap = new HashMap<>();
		String word, str;
		int cnt = 0, size = strs.length;
		for (int i = 0; i < size; i++) {
			word = "";
			if (strs[i] != null) {
				if (!strs[i].startsWith(" "))
					for (int j = 0; j < strs[i].length(); j++) {
						str = strs[i];
						if (String.valueOf(str.charAt(j)).equals(" ") || String.valueOf(str.charAt(j)).equals("에")
								|| String.valueOf(str.charAt(j)).equals("입니")
								|| String.valueOf(str.charAt(j)).equals("있")
								|| String.valueOf(str.charAt(j)).equals("%다")) {
							if (word.length() > 1) {
								wordsMap.put(word, wordsMap.getOrDefault(word, 0) + 1);
							}
							word = "";
						} else {
							word += String.valueOf(str.charAt(j));
						}
					}
			} else {
			}
		}
		List<Map.Entry<String, Integer>> entryList = new LinkedList<>(wordsMap.entrySet());
		entryList.sort(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});
		for (Map.Entry<String, Integer> entry : entryList) {
			if (cnt < 10) {
				popularWords[cnt] = entry.getKey();
			}
			cnt++;
		}
		for (int i = 0; i < popularWords.length; i++) {
			if (popularWords[i] != null)
				receiveListModel.addElement(i + 1 + " : " + popularWords[i]);
		}
		wordsMap.clear();
		return popularWords;
	}

	public static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c1 = Calendar.getInstance();
		String strToday = sdf.format(c1.getTime());
		return strToday;
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			keyword = receiveList.getSelectedValue().toString();
			if (receiveList.getSelectedIndex() != receiveList.getLastVisibleIndex())
				keyword = keyword.substring(4);
			else
				keyword = keyword.substring(5);
		} catch (NullPointerException e1) {

		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			openKeyword();
		}
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
	}

	public void openKeyword() {
		try {
			Desktop.getDesktop()
					.browse(new URI("https://search.naver.com/search.naver?where=news&sm=tab_jum&query=" + keyword));
		} catch (Exception e) {

		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("search")) {
			search();
		}
	}

	public static void main(String[] args) {
		new HotKeywords();
	}
}