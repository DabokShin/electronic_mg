package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Locale;

public class UserVO implements Serializable {

	/**
	 *  사용자 인덱스
	 */
	@SerializedName("idx")
	@Expose
	private int idx;

	/**
	 *  이메일
	 */
	@SerializedName("email")
	@Expose
	private String email;

	/**
	 *  지역
	 */
	@SerializedName("area")
	@Expose
	private int area;

	/**
	 *  생년
	 */
	@SerializedName("year")
	@Expose
	private int year;

	/**
	 *  성별
	 */
	@SerializedName("sex")
	@Expose
	private String sex;

	/**
	 *  보유 캐시백(현재)
	 */
	@SerializedName("saving")
	@Expose
	private int saving;


	/**
	 *  푸시여부
	 */
	@SerializedName("pushyn")
	@Expose
	private String pushyn;

	/**
	 *  탈퇴여부
	 */
	@SerializedName("byeyn")
	@Expose
	private String byeyn;

	/**
	 *  탈퇴일
	 */
	@SerializedName("byedate")
	@Expose
	private Object byedate;

	/**
	 *  가입일
	 */
	@SerializedName("hidate")
	@Expose
	private String hidate;

	/**
	 *  UUID
	 */
	@SerializedName("uuid")
	@Expose
	private String uuid;

	/**
	 *  통신사
	 */
	@SerializedName("tcom")
	@Expose
	private String tcom;

	/**
	 *  운영체제
	 */
	@SerializedName("os")
	@Expose
	private String os;

	/**
	 *  푸시토큰
	 */
	@SerializedName("token")
	@Expose
	private String token;

	/**
	 *  가입 sns
	 */
	@SerializedName("sns")
	@Expose
	private String sns;

	/**
	 *  sns 아이디
	 */
	@SerializedName("snsid")
	@Expose
	private String snsid;

	/**
	 *  사용자 닉네임
	 */
	@SerializedName("nick")
	@Expose
	private String nick;

	/**
	 *  첨부파일
	 */
	@SerializedName("attaches")
	@Expose
	private Object attaches;

	/**
	 *  손잡이
	 */
	@SerializedName("finger")
	@Expose
	private String finger;

	/**
	 *  입금은행 코드
	 */
	@SerializedName("bank")
	@Expose
	private String bank;

	/**
	 *  입금계좌
	 */
	@SerializedName("account")
	@Expose
	private String account;

	/**
	 *  입금계좌 보안
	 */
	@SerializedName("exaccount")
	@Expose
	private String exaccount;

	/**
	 *  송금실패 여부
	 */
	@SerializedName("transferyn")
	@Expose
	private String transferyn;

	/**
	 *  로그인 경로
	 */
	@SerializedName("route")
	@Expose
	private String route;

	/**
	 *  입금계좌명
	 */
	@SerializedName("accountname")
	@Expose
	private String accountname;

	/**
	 *  가입경로
	 */
	@SerializedName("channel")
	@Expose
	private String channel;

	/**
	 *  최종 로그인 시간
	 */
	@SerializedName("logindate")
	@Expose
	private String logindate;

	/**
	 *  가맹점 유니크 ID
	 */
	@SerializedName("cid")
	@Expose
	private String cid;

	private String advid;

	public String getKidx()
	{
		return String.format(Locale.getDefault(), "K%d", idx);
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public int getSaving() {
		return saving;
	}

	public void setSaving(int saving) {
		this.saving = saving;
	}

	public String getPushyn() {
		return pushyn;
	}

	public void setPushyn(String pushyn) {
		this.pushyn = pushyn;
	}

	public String getByeyn() {
		return byeyn;
	}

	public void setByeyn(String byeyn) {
		this.byeyn = byeyn;
	}

	public Object getByedate() {
		return byedate;
	}

	public void setByedate(Object byedate) {
		this.byedate = byedate;
	}

	public String getHidate() {
		return hidate;
	}

	public void setHidate(String hidate) {
		this.hidate = hidate;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTcom() {
		return tcom;
	}

	public void setTcom(String tcom) {
		this.tcom = tcom;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSns() {
		return sns;
	}

	public void setSns(String sns) {
		this.sns = sns;
	}

	public String getSnsid() {
		return snsid;
	}

	public void setSnsid(String snsid) {
		this.snsid = snsid;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public Object getAttaches() {
		return attaches;
	}

	public void setAttaches(Object attaches) {
		this.attaches = attaches;
	}

	public String getFinger() {
		return finger;
	}

	public void setFinger(String finger) {
		this.finger = finger;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getExaccount() {
		return exaccount;
	}

	public void setExaccount(String exaccount) {
		this.exaccount = exaccount;
	}

	public String getTransferyn() {
		return transferyn;
	}

	public void setTransferyn(String transferyn) {
		this.transferyn = transferyn;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getAccountname() {
		return accountname;
	}

	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getLogindate() {
		return logindate;
	}

	public void setLogindate(String logindate) {
		this.logindate = logindate;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getAdvid() {
		return advid;
	}

	public void setAdvid(String advid) {
		this.advid = advid;
	}
}
