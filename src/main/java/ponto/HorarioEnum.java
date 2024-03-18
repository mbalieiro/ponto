package ponto;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum HorarioEnum {
	INICIO_JORNADA("09:00"),
	INICIO_MEIA_JORNADA("12:00"),
	INICIO_ALMOCO("12:00"),
	FIM_ALMOCO("13:00"),
	FIM_JORNADA("18:00"),
	FIM_MEIA_JORNADA("18:00"),
	MIN_HORA_EXTRA_NOTURNA("22:00"),
	MAX_HORA_EXTRA_NOTURNA("05:00");
	
	static DateFormat df = new SimpleDateFormat("HH:mm");
	
	String hora;
	
	private HorarioEnum(String hora) {
		this.hora = hora;
	}
	
	public Date getHora() {
		try {
			return df.parse(hora);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}