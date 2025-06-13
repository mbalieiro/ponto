package ponto;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum HorarioEnum {
	INICIO_SOBREAVISO_T1("18:30", false),
	FIM_SOBREAVISO_T1("07:30", true),
	INICIO_SOBREAVISO_T2("07:30", false),
	FIM_SOBREAVISO_T2("07:30", true),
	INICIO_SOBREAVISO_T3("22:00", false),
	FIM_SOBREAVISO_T3("06:00", true),
	
	INICIO_JORNADA("09:00", false),
	FIM_JORNADA("18:00", false),
	INICIO_MEIA_JORNADA("12:00", false),
	FIM_MEIA_JORNADA("18:00", false),
	INICIO_ALMOCO("12:00", false),
	FIM_ALMOCO("13:00", false),
	INICIO_HORA_EXTRA_NOTURNA("22:00", false),
	FIM_HORA_EXTRA_NOTURNA("05:00", true),
	;
	
	static DateFormat df = new SimpleDateFormat("HH:mm");
	
	private String hora;
	private boolean proximoDia;
	
	private HorarioEnum(String hora, boolean proximoDia) {
		this.hora = hora;
		this.proximoDia = proximoDia;
	}
	
	public Date getHora() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(hora));
			cal.add(Calendar.DAY_OF_YEAR, this.proximoDia ? 1 : 0);
			return cal.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}