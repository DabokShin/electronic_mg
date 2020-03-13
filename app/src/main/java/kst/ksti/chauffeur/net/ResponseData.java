package kst.ksti.chauffeur.net;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseData<T> {
	@SerializedName("resultCode")
	@Expose
	private String  resultCode = "";

	@SerializedName("resultMessage")
	@Expose
	private String resultMessage = "";


	@SerializedName("detailMessage")
	@Expose
	private String detailMessage = "";


	@SerializedName("data")
	@Expose
	private T data;

	@SerializedName("list")
	@Expose
	private List<T> list;

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getError() {
		return resultMessage;
	}

	public void setError(String error) {
		this.resultMessage = error;
	}

	public String getMessage() {
		return detailMessage;
	}

	public void setMessage(String message) {
		this.detailMessage = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public List<T> getList(){
		return list;
	}

	public void setList(List<T> list){
		this.list = list;
	}
}
