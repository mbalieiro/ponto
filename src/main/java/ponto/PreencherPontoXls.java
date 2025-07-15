package ponto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvToBeanFilter;
import com.opencsv.exceptions.CsvException;

import de.jollyday.HolidayManager;
import de.jollyday.HolidayType;
import de.jollyday.ManagerParameter;
import de.jollyday.parameter.CalendarPartManagerParameter;

public class PreencherPontoXls implements CsvToBeanFilter {
	private static DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat df2 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
	private static DateFormat df3 = new SimpleDateFormat("EEEE");
	private static DateFormat df4 = new SimpleDateFormat("MM-yyyy");
	private static DateFormat df5 = new SimpleDateFormat("kk:mm");
	
	private Funcionario funcionario;

	private List<Complemento> complementos;
	
	private boolean feriado = false;
	private boolean domingo = false;
	
	private int periodosHoraExtra;
	private long totalHoraExtra50 = 0;
	private long totalHoraExtra100 = 0;
	private long totalHoraExtraNoturna50 = 0;
	private long totalHoraExtraNoturna100 = 0;
	private long totalSobreaviso = 0;

	private XWPFDocument doc;

	public PreencherPontoXls() throws FileNotFoundException, IOException, CsvException {

		try (InputStream input = this.getClass().getResourceAsStream("/FQ109-115_v_12.docx")) {
			this.doc = new XWPFDocument(input);
		}
		
		try (InputStream input = this.getClass().getResourceAsStream("/dados_funcionario.csv")) {
			this.funcionario = new CsvToBeanBuilder<Funcionario>(new InputStreamReader(input))
					.withSeparator(';')
					.withType(Funcionario.class)
					.withFilter(this)
					.build()
					.parse()
					.stream()
					.findFirst()
					.orElseThrow(UnsupportedOperationException::new);
		}

		try (InputStream input = this.getClass().getResourceAsStream("/dados_complementares.csv")) {
			this.complementos = new CsvToBeanBuilder<Complemento>(new InputStreamReader(input))
					.withSeparator(';')
					.withType(Complemento.class)
					.withFilter(this)
					.build()
					.parse();
		}
	}
	
	@Override
	public boolean allowLine(String[] line) {
		return line.length > 0 && !StringUtils.isBlank(line[0]);
	}

	private void setText(XWPFTableCell cell, String text, int size, boolean bold) {
		XWPFParagraph par = cell.getParagraphArray(0);

		while (!par.runsIsEmpty()) {
			par.removeRun(0);
		}

		XWPFRun run = par.createRun();
		run.setText(text);
		run.setFontFamily("Arial");
		run.setFontSize(size);
		run.setBold(bold);
	}

	public void preencher() throws ParseException {
		ManagerParameter parameter = new CalendarPartManagerParameter("br_pa", null);
		HolidayManager m = HolidayManager.getInstance(parameter);
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();

		inicio.setTime(this.funcionario.getData());
		fim.setTime(this.funcionario.getData());

		inicio.add(Calendar.DAY_OF_MONTH, 1);
		inicio.add(Calendar.MONTH, -1);

		this.doc.getTables().stream().filter(t -> t.getRow(0).getCell(0).getText().startsWith("LOCAL")).forEach(t -> {
			t.getRows().stream().forEach(r -> {
				r.getTableCells().forEach(c -> {
					if (c.getText().startsWith("LOCAL E DATA")) {
						setText(c, "LOCAL E DATA: " + this.funcionario.getLocal() + ", " + df2.format(fim.getTime()), 6, true);
					} else if (c.getText().startsWith("PERÍODO:")) {
						setText(c, "PERÍODO: " + df1.format(inicio.getTime()) + " A " + df1.format(fim.getTime()), 6, true);
					} else if (c.getText().startsWith("MATRÍCULA")) {
						setText(c, "MATRÍCULA: " + this.funcionario.getMatricula(), 6, true);
					} else if (c.getText().startsWith("EMPREGADO")) {
						setText(c, "EMPREGADO: " + this.funcionario.getEmpregado(), 6, true);
					} else if (c.getText().startsWith("UOR")) {
						setText(c, "UOR: " + this.funcionario.getUor(), 6, true);
					} else if (c.getText().startsWith("FUNÇÃO")) {
						setText(c, "FUNÇÃO: " + this.funcionario.getFuncao(), 6, true);
					} else if (c.getText().startsWith("HORÁRIO")) {
						setText(c, "HORÁRIO: " + this.funcionario.getHorario(), 6, true);
					}
				});
			});
		});
		
		this.doc.getTables().stream().filter(t -> t.getRow(0).getCell(0).getText().startsWith("DATA")).forEach(t -> {
			Calendar dia = Calendar.getInstance();
			dia.setTime(inicio.getTime());
			
			for (int i = 2; i < 33; i++) {
				XWPFTableRow linha = t.getRow(i);
				
				this.feriado = false;
				this.domingo = false;
				this.periodosHoraExtra = 0;
				
				if (dia.after(fim)) {
					for (int j = 0; j < linha.getTableCells().size(); j++) {
						XWPFTableCell c = linha.getCell(j);
						c.getCTTc().addNewTcPr().addNewShd().setFill("auto");
						setText(c, "", 4, false);
					}
				} else {
					for (int j = 0; j < linha.getTableCells().size(); j++) {
						XWPFTableCell c = linha.getCell(j);
						if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || dia.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
							c.getCTTc().addNewTcPr().addNewShd().setFill("E7E6E6");
						} else {
							c.getCTTc().addNewTcPr().addNewShd().setFill("auto");
						}
					}

					XWPFTableCell data = linha.getCell(0);
					XWPFTableCell semana = linha.getCell(1);

					setText(data, df1.format(dia.getTime()), 4, true);
					setText(semana, df3.format(dia.getTime()), 4, false);
					
					if (dia.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { // Dias Uteis
						if (!m.isHoliday(dia, HolidayType.OFFICIAL_HOLIDAY, "pa", "bel")) {
							if (m.isHoliday(dia, HolidayType.OFFICIAL_HOLIDAY, "pa", "bel", "half")) { // Meio expediente
								setText(linha.getTableCells().get(2), Util.obterHora(HorarioEnum.INICIO_MEIA_JORNADA), 4, false);
								setText(linha.getTableCells().get(3), "", 4, false);
								setText(linha.getTableCells().get(4), "", 4, false);
								setText(linha.getTableCells().get(5), Util.obterHora(HorarioEnum.FIM_MEIA_JORNADA), 4, false);
							} else { // Dia Normal
								if (dia.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
									if (this.complementos.stream().filter(c -> c.getData().equals(dia.getTime()) 
											&& (c.getCategoria() == CategoriaEnum.FERIAS || c.getCategoria() == CategoriaEnum.OUTROS)).count() == 0) { // Não férias nem outros
										setText(linha.getTableCells().get(2), Util.obterHora(HorarioEnum.INICIO_JORNADA), 4, false);
										setText(linha.getTableCells().get(3), Util.obterHora(HorarioEnum.INICIO_ALMOCO), 4, false);
										setText(linha.getTableCells().get(4), Util.obterHora(HorarioEnum.FIM_ALMOCO), 4, false);
										setText(linha.getTableCells().get(5), Util.obterHora(HorarioEnum.FIM_JORNADA), 4, false);
									}
								}
							}
						} else {// Feriados
							setText(linha.getTableCells().get(2), "FERIADO", 4, true);
							this.feriado = true;
						}
					} else {
						this.domingo = true;
					}
					
					this.complementos.stream().filter(c -> c.getData().equals(dia.getTime())).forEach(c -> {
						
						long somaHorasExtras = this.complementos.stream()
								.filter(c1 -> c1.getData().equals(dia.getTime()) && c1.getCategoria() == CategoriaEnum.HORA_EXTRA)
								.mapToInt(c1 -> {
									long diff = c1.getFim().getTime() - (c1.getFim().after(c1.getInicio()) ? c1.getInicio().getTime() : c1.getInicio().getTime() - TimeUnit.HOURS.toMillis(24));
									long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
									return (int) minutes;
								}).sum();
						
						switch (c.getCategoria()) {
						case HORA_EXTRA:
							switch (this.periodosHoraExtra) {
							case 0:
								setText(linha.getTableCells().get(6), df5.format(c.getInicio()), 4, false);
								setText(linha.getTableCells().get(7), df5.format(c.getFim()), 4, false);
								break;
							case 1:
								setText(linha.getTableCells().get(8), df5.format(c.getInicio()), 4, false);
								setText(linha.getTableCells().get(9), df5.format(c.getFim()), 4, false);
								break;
							default:
								throw new UnsupportedOperationException("Só podem haver, no máximo, 2 períodos de horas-extra por dia");
							}
							
							if (c.getFim().before(c.getInicio())) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(c.getFim());
								cal.add(Calendar.DAY_OF_YEAR, 1);
								c.setFim(cal.getTime());
							}
							
							long inicioPeriodo = 0;
							long fimPeriodo = 0;
							if (isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = c.getInicio().getTime();
								fimPeriodo = c.getFim().getTime();
							} else if (isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && !isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = c.getInicio().getTime();
								fimPeriodo = HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora().getTime();
							} else if (!isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora().getTime();
								fimPeriodo = c.getFim().getTime();
							} else if (!isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && !isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								if (isDateBetween(HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), c.getInicio(), c.getFim())) {
									inicioPeriodo = HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora().getTime();
									fimPeriodo = HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora().getTime();
								}
							}
							
							if (inicioPeriodo > 0 && fimPeriodo > 0) {// Possui hora extra noturna
								if (!this.feriado && !this.domingo) {// Hora Extra 50%
									this.totalHoraExtraNoturna50 += fimPeriodo - inicioPeriodo;
								} else {// Hora Extra 100%
									this.totalHoraExtraNoturna100 += fimPeriodo - inicioPeriodo;
								}
							}
							
							inicioPeriodo = 0;
							fimPeriodo = 0;
							if (!isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = c.getInicio().getTime();
								fimPeriodo = HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora().getTime();
							} else if (isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && !isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora().getTime();
								fimPeriodo = c.getFim().getTime();
							} else if (!isDateBetween(c.getInicio(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora()) && !isDateBetween(c.getFim(), HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora())) {
								inicioPeriodo = c.getInicio().getTime();
								if (isDateBetween(HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora(), c.getInicio(), c.getFim())) {
									fimPeriodo = c.getFim().getTime() - (HorarioEnum.FIM_HORA_EXTRA_NOTURNA.getHora().getTime() - HorarioEnum.INICIO_HORA_EXTRA_NOTURNA.getHora().getTime());
								} else {
									fimPeriodo = c.getFim().getTime();
								}
							}
							
							if (inicioPeriodo > 0 && fimPeriodo > 0) {// Possui hora extra não noturna
								if (!this.feriado && !this.domingo) {// Hora Extra 50%
									this.totalHoraExtra50 += fimPeriodo - inicioPeriodo;
								} else {// Hora Extra 100%
									this.totalHoraExtra100 += fimPeriodo - inicioPeriodo;
								}
							}
							
							periodosHoraExtra++;
							break;
						case SOBREAVISO_T1:
							
							Calendar inicioT1 = Calendar.getInstance();
							inicioT1.setTime(HorarioEnum.INICIO_SOBREAVISO_T1.getHora());
							Calendar fimT1 = Calendar.getInstance();
							fimT1.setTime(HorarioEnum.FIM_SOBREAVISO_T1.getHora());
							
							if (inicioT1.after(fimT1)) {
								fimT1.add(Calendar.DAY_OF_MONTH, 1);
							}
							
							int duracaoT1 = getDiferencaEmHoras(inicioT1.getTime(), fimT1.getTime());
							
							Calendar t1 = Calendar.getInstance();
							t1.setTimeInMillis(0);
							t1.set(Calendar.HOUR_OF_DAY, duracaoT1);
							t1.set(Calendar.MINUTE, 0);
							t1.set(Calendar.SECOND, 0);
							t1.add(Calendar.MINUTE, (int) -somaHorasExtras);
							
							setText(linha.getTableCells().get(10), df5.format(t1.getTime()), 4, false);
							this.totalSobreaviso += TimeUnit.HOURS.toMillis(duracaoT1) - TimeUnit.MINUTES.toMillis(somaHorasExtras);
							break;
						case SOBREAVISO_T2:
							
							Calendar inicioT2 = Calendar.getInstance();
							inicioT2.setTime(HorarioEnum.INICIO_SOBREAVISO_T2.getHora());
							Calendar fimT2 = Calendar.getInstance();
							fimT2.setTime(HorarioEnum.FIM_SOBREAVISO_T2.getHora());
							
							if (inicioT2.equals(fimT2) || inicioT2.after(fimT2)) {
								fimT2.add(Calendar.DAY_OF_MONTH, 1);
							}
							
							int duracaoT2 = getDiferencaEmHoras(inicioT2.getTime(), fimT2.getTime());
							
							Calendar t2 = Calendar.getInstance();
							t2.setTimeInMillis(0);
							t2.set(Calendar.HOUR_OF_DAY, duracaoT2);
							t2.set(Calendar.MINUTE, 0);
							t2.set(Calendar.SECOND, 0);
							t2.add(Calendar.MINUTE, (int) -somaHorasExtras);
							
							setText(linha.getTableCells().get(10), df5.format(t2.getTime()), 4, false);
							this.totalSobreaviso += TimeUnit.HOURS.toMillis(duracaoT2) - TimeUnit.MINUTES.toMillis(somaHorasExtras);
							break;
						case SOBREAVISO_T3:
							
							Calendar inicioT3 = Calendar.getInstance();
							inicioT3.setTime(HorarioEnum.INICIO_SOBREAVISO_T3.getHora());
							Calendar fimT3 = Calendar.getInstance();
							fimT3.setTime(HorarioEnum.FIM_SOBREAVISO_T3.getHora());
							
							if (inicioT3.after(fimT3)) {
								fimT3.add(Calendar.DAY_OF_MONTH, 1);
							}
							
							int duracaoT3 = getDiferencaEmHoras(inicioT3.getTime(), fimT3.getTime());
							
							Calendar t3 = Calendar.getInstance();
							t3.setTimeInMillis(0);
							t3.set(Calendar.HOUR_OF_DAY, duracaoT3);
							t3.set(Calendar.MINUTE, 0);
							t3.set(Calendar.SECOND, 0);
							t3.add(Calendar.MINUTE, (int) -somaHorasExtras);
							
							setText(linha.getTableCells().get(10), df5.format(t3.getTime()), 4, false);
							this.totalSobreaviso += TimeUnit.HOURS.toMillis(duracaoT3) - TimeUnit.MINUTES.toMillis(somaHorasExtras);
							break;
						case FERIAS:
							setText(linha.getTableCells().get(2), "FÉRIAS", 4, true);
							setText(linha.getTableCells().get(3), "", 4, false);
							setText(linha.getTableCells().get(4), "", 4, false);
							setText(linha.getTableCells().get(5), "", 4, false);
							break;
						case OUTROS:
							if (c.getInicio() != null && c.getFim() != null) {
								setText(linha.getTableCells().get(2), df5.format(c.getInicio()), 4, false);
								setText(linha.getTableCells().get(3), "", 4, false);
								setText(linha.getTableCells().get(4), "", 4, false);
								setText(linha.getTableCells().get(5), df5.format(c.getFim()), 4, false);
							}
							break;
						}
						
						if (c.getDescricao() != null) {
							setText(linha.getTableCells().get(14), c.getDescricao(), 4, true);
						}
					});
				}

				dia.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			// Totalizadores
			XWPFTableRow linha = t.getRow(36);
			setText(linha.getTableCells().get(1), DurationFormatUtils.formatPeriod(0, this.totalHoraExtraNoturna50, "HH:mm"), 6, true);
			setText(linha.getTableCells().get(2), DurationFormatUtils.formatPeriod(0, this.totalHoraExtraNoturna100, "HH:mm"), 6, true);
			setText(linha.getTableCells().get(3), DurationFormatUtils.formatPeriod(0, this.totalHoraExtra50, "HH:mm"), 6, true);
			setText(linha.getTableCells().get(4), DurationFormatUtils.formatPeriod(0, this.totalHoraExtra100, "HH:mm"), 6, true);
			setText(linha.getTableCells().get(10), DurationFormatUtils.formatPeriod(0, this.totalSobreaviso, "HH:mm"), 6, true);
		});
	}

	public void salvar() throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.funcionario.getData());

		String caminho = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		
		String novoNome = caminho + "/" + this.funcionario.getEmpregado() + " - " + df4.format(cal.getTime()) + ".docx";
		
		try (FileOutputStream out = new FileOutputStream(novoNome)) {
			doc.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isDateBetween(Date d, Date a, Date b) {
		if (a == null) {
		    return b == null || d.compareTo(b) <= 0;
		} else if (b == null) {
		    return a.compareTo(d) <= 0;
		} else {
		    return a.compareTo(d) * d.compareTo(b) >= 0;
		}
	}
	
	public static int getDiferencaEmHoras(Date baseDate, Date currentDate) {

		long m1 = baseDate.getTime();
		long m2 = currentDate.getTime();
		long horas = (m2 - m1) / (60 * 60 * 1000);
		long minutos = (m2 - m1) / (60 * 1000);
		long minutosFormatado = ((horas * 60) + minutos) - (horas * 60);
		return (int) (minutosFormatado / 60);
	}

	/**
	 * Code to test updating of the embedded Excel workbook.
	 * 
	 * @throws ParseException
	 */
	public static void main(String[] args) throws Exception {
		PreencherPontoXls ued = new PreencherPontoXls();
		ued.preencher();
		ued.salvar();
	}
}