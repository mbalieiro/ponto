package ponto;

import java.util.Date;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

public class Funcionario {
	
	@CsvBindByName
	private Integer matricula;
	
	@CsvBindByName
	private String empregado;
	
	@CsvBindByName
	private Integer uor;
	
	@CsvBindByName
	private String funcao;
	
	@CsvBindByName
	private String horario;
	
	@CsvBindByName
	private String local;
	
	@CsvDate(value = "dd/MM/yyyy")
	@CsvBindByName
	private Date data;

	public Integer getMatricula() {
		return matricula;
	}

	public void setMatricula(Integer matricula) {
		this.matricula = matricula;
	}

	public String getEmpregado() {
		return empregado;
	}

	public void setEmpregado(String empregado) {
		this.empregado = empregado;
	}

	public Integer getUor() {
		return uor;
	}

	public void setUor(Integer uor) {
		this.uor = uor;
	}

	public String getFuncao() {
		return funcao;
	}

	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Funcionario [matricula=" + matricula + ", empregado=" + empregado + ", uor=" + uor + ", funcao=" + funcao + ", local=" + local + ", data=" + data + "]";
	}
}