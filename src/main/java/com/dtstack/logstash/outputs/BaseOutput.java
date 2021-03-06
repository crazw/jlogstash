package com.dtstack.logstash.outputs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dtstack.logstash.render.FreeMarkerRender;
import com.dtstack.logstash.render.TemplateRender;

/**
 * 
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:27:30
 * Company: www.dtstack.com
 * @author sishu.yss
 *
 */
public abstract class BaseOutput implements Cloneable, java.io.Serializable{

	private static final long serialVersionUID = -1613159084286522811L;

	private static final Logger logger = LoggerFactory.getLogger(BaseOutput.class);

	protected Map config;
	
	protected List<TemplateRender> IF;
	
	//0未提交，1提交成功，2提交失败
	protected AtomicInteger ato = new AtomicInteger(0);
	
	//数据强一致性是否开启
	protected static boolean consistency =false;

	public BaseOutput(Map config) {
		this.config = config;

		if (this.config.containsKey("if")) {
			IF = new ArrayList<TemplateRender>();
			for (String c : (List<String>) this.config.get("if")) {
				try {
					IF.add(new FreeMarkerRender(c, c));
				} catch (IOException e) {
					logger.error(e.getMessage());
					System.exit(1);
				}
			}
		} else {
			IF = null;
		}
	}

	public abstract void prepare();

	protected abstract void emit(Map event);
	
	public void process(Map event) {
		if(event != null && event.size() > 0){
			boolean succuess = true;
			if (this.IF != null) {
				for (TemplateRender render : this.IF) {
					if (!render.render(event).equals("true")) {
						succuess = false;
						break;
					}
				}
			}
			if (succuess == true) {
				this.emit(event);
			}
		}
	}
	
	
    public AtomicInteger getAto() {
		return ato;
	}


	public static boolean isConsistency() {
		return consistency;
	}


	@Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    
}
