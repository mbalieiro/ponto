package ponto;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Util {
	private static DateFormat DF1 = new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat DF2 = new SimpleDateFormat("HH:mm");
	
	private static Random RAND = new Random();
	private static Map<HorarioEnum, Date> ULTIMOS_HORARIOS = new HashMap<>();
	
	private static Integer TOLERANCIA = 6;
	private static Integer MINIMO_ALMOCO = 1 * 60;
	private static Integer MINIMO_MEIO_EXPEDIENTE = 6 * 60;
	
	public static Date parseData(String data) {
		try {
			return DF1.parse(data.trim());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String obterHora(HorarioEnum horario) {
		Calendar cal = Calendar.getInstance();
		int delta;
		
		switch (horario) {
		case FIM_ALMOCO:
			cal.setTime(ULTIMOS_HORARIOS.get(HorarioEnum.INICIO_ALMOCO));
			delta = MINIMO_ALMOCO + RAND.nextInt(TOLERANCIA);
			cal.add(Calendar.MINUTE, delta);
			break;
		case FIM_MEIA_JORNADA:
			cal.setTime(ULTIMOS_HORARIOS.get(HorarioEnum.INICIO_MEIA_JORNADA));
			delta = MINIMO_MEIO_EXPEDIENTE + RAND.nextInt(TOLERANCIA);
			cal.add(Calendar.MINUTE, delta);
			break;
		default:
			cal.setTime(horario.getHora());
			delta = TOLERANCIA - RAND.nextInt(TOLERANCIA * 2);
			cal.add(Calendar.MINUTE, delta);
			break;
		}
		
		ULTIMOS_HORARIOS.put(horario, cal.getTime());
		return DF2.format(cal.getTime());
	}
}