package ponto;

import java.util.Date;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

public class Complemento {
	
	@CsvDate(value = "dd/MM/yyyy")
	@CsvBindByName
	private Date data;
	
	@CsvBindByName
	private CategoriaEnum categoria;
	
	@CsvDate(value = "HH:mm")
	@CsvBindByName
	private Date inicio;
	
	@CsvDate(value = "HH:mm")
	@CsvBindByName
	private Date fim;
	
	@CsvBindByName(column = "tab_109")
	private String descricao;
	
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public CategoriaEnum getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaEnum categoria) {
		this.categoria = categoria;
	}

	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return "Complemento [data=" + data + ", categoria=" + categoria + ", inicio=" + inicio + ", fim=" + fim + ", descricao=" + descricao + "]";
	}
}