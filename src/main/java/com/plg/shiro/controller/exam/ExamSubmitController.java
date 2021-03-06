package com.plg.shiro.controller.exam;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.plg.shiro.entity.OmExamAnswer;
import com.plg.shiro.entity.OmExamPlanVo;
import com.plg.shiro.entity.OmExamSubmit;
import com.plg.shiro.entity.OmUser;
import com.plg.shiro.entity.Vo.OmExamSubmitExportVo;
import com.plg.shiro.entity.Vo.OmExamSubmitVo;
import com.plg.shiro.service.IExamPlanService;
import com.plg.shiro.service.IExamSubmitService;
import com.plg.shiro.service.IPaperService;
import com.plg.shiro.service.IUserGroupService;
import com.plg.shiro.service.IUserService;
import com.plg.shiro.util.AjaxUtil;
import com.plg.shiro.util.ExcelUtils;
import com.plg.shiro.util.dwz.AjaxObject;
import com.plg.shiro.util.dwz.LayuiPage;

import jxl.Workbook;
import jxl.write.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *试卷答卷提交、阅卷管理
 */
@Controller
@RequestMapping({ "/admin/exam/omExamSubmit" })
public class ExamSubmitController {
	
	private static Logger logger = LoggerFactory.getLogger(ExamSubmitController.class);

	@Resource
	private IExamSubmitService service;
	
	@Resource
	private IExamPlanService planService;
	
	@Resource
	private IPaperService paperService;
	
	@Resource
	private IUserService userService;
	
	@Resource
	private IUserGroupService groupService;
	
	private static final String EXAMSUBMIT_PATH = "admin/exam/omExamSubmit/";
	
	
	/**
	 * 列表页
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/markList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String list(HttpServletRequest request) {
		//阅卷列表
		request.setAttribute("status", "3");
		return EXAMSUBMIT_PATH+"markList";
	}
	
	@RequestMapping(value = { "/historyList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String historyList(HttpServletRequest request) {
		return EXAMSUBMIT_PATH+"historyList";
	}
	
	@RequestMapping(value = { "/gradeList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String gradeList(HttpServletRequest request) {
		List<OmExamPlanVo> planList = planService.selectVoByStatus("2,3");//2正在进行,3考试结束的
		request.setAttribute("planList", planList);
		return EXAMSUBMIT_PATH+"gradeList";
	}
	
	@RequestMapping(value = { "/mygradeList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String mygradeList(HttpServletRequest request) {
		return EXAMSUBMIT_PATH+"mygradeList";
	}
	
	@RequestMapping(value = { "/monitorList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String monitorList(HttpServletRequest request) {
		return EXAMSUBMIT_PATH+"monitorList";
	}
	
	//获取列表数据
  	@RequestMapping("getList")
    @ResponseBody
  	public AjaxObject getList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				List<OmExamSubmitVo> list = service.findVoPageList(request, page);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
     //获取列表数据(回顾历史考试数据 考试人员)
  	@RequestMapping("getHistoryList")
    @ResponseBody
  	public AjaxObject getHistoryList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				OmUser omUser = (OmUser) SecurityUtils.getSubject().getSession().getAttribute("om_user");
  				String examUserType = "1";//授权考试人员
  				String status = "4";//成绩发布状态
  				List<OmExamSubmitVo> list = service.findUserPageList(request, page,omUser,examUserType,status);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
    //获取列表数据(人工评卷数据 阅卷人员)
  	@RequestMapping("getMarkList")
    @ResponseBody
  	public AjaxObject getMarkList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				OmUser omUser = (OmUser) SecurityUtils.getSubject().getSession().getAttribute("om_user");
  				String examUserType = "2";//授权阅卷人员
  				String status = "2";//提交试卷待批阅状态
  				List<OmExamSubmitVo> list = service.findUserPageList(request, page,omUser,examUserType,status);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
    //获取列表数据(成绩列表 发布成绩)
  	@RequestMapping("getGradeList")
    @ResponseBody
  	public AjaxObject getGradeList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				String status = "3,4";//阅卷完成的试卷和成绩发布的试卷
  				List<OmExamSubmitVo> list = service.findUserPageList(request, page,status);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
  	//获取列表数据(监控列表)
  	@RequestMapping("getMonitorList")
    @ResponseBody
  	public AjaxObject getMonitorList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				String status = "1,2";//开始答题的试卷和提交答卷的试卷
  				List<OmExamSubmitVo> list = service.findUserPageList(request, page,status);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
    //考试提交结果
    @RequestMapping("saveExamSubmit")
    @ResponseBody
	public AjaxObject saveExamSubmit(HttpServletRequest request,OmExamSubmit bean){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			service.saveExamSubmit(bean);
			result.setData(bean);
		} catch (Exception e) {
			logger.error("保存考试结果信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}

	
    
    //阅卷
    @RequestMapping("saveMarkExam")
    @ResponseBody
	public AjaxObject saveMarkExam(HttpServletRequest request,OmExamSubmit bean,String omExamAnswerListJson){
    	AjaxObject result = AjaxObject.newOk("success");
    	try {
    		List<OmExamAnswer> omExamAnswerList = null;
			if (StringUtils.isNotBlank(omExamAnswerListJson)) {
				omExamAnswerList = AjaxUtil.getJavaCollection(new OmExamAnswer(), omExamAnswerListJson);
			}
			service.saveMarkExam(bean,omExamAnswerList);
		} catch (Exception e) {
			logger.error("保存阅卷结果信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
    	return result;
    }
    
    //更改答卷
    @RequestMapping("editResultExam")
    @ResponseBody
	public AjaxObject editResultExam(HttpServletRequest request,OmExamSubmit bean,String omExamAnswerListJson){
    	AjaxObject result = AjaxObject.newOk("success");
    	try {
    		List<OmExamAnswer> omExamAnswerList = null;
			if (StringUtils.isNotBlank(omExamAnswerListJson)) {
				omExamAnswerList = AjaxUtil.getJavaCollection(new OmExamAnswer(), omExamAnswerListJson);
			}
			service.editResultExam(bean,omExamAnswerList);
		} catch (Exception e) {
			logger.error("更改答卷结果信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
    	return result;
    }
    
    //发布成绩
    @RequestMapping("gradeExam")
    @ResponseBody
	public AjaxObject gradeExam(HttpServletRequest request,String ids){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			String status = "4";//发布成绩
			service.updateBatch(ids,"4");
		} catch (Exception e) {
			logger.error("发布成绩："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
    //批量删除操作
    @RequestMapping("del")
    @ResponseBody
	public AjaxObject del(HttpServletRequest request,String ids){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			service.deleteBatch(ids);
		} catch (Exception e) {
			logger.error("删除成绩信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
	@RequestMapping("/exportGrade")
	@SuppressWarnings("deprecation")
	@ResponseBody
	public void exportGrade(HttpServletRequest request,HttpServletResponse response)throws Exception {
		String file = "成绩结果信息.xls";
		response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode(file, "UTF-8") );
		String error = null;
		String selectSql = null; 
		try {
			String status = "3,4";//阅卷完成的试卷和成绩发布的试卷
			List<OmExamSubmitVo> list = service.getList(request,status);
			WritableWorkbook wwb = Workbook.createWorkbook(response.getOutputStream());
			if(list==null||list.size()==0){
				WritableSheet sheet = wwb.createSheet("暂无符合条件记录",0);
				WritableCellFormat wc = new WritableCellFormat();
			    wc.setAlignment(Alignment.CENTRE); // 设置居中
			    sheet.addCell(new Label(0, 0, "暂无符合条件记录",wc));
			    wwb.write();
				wwb.close();
				return;
			}
				String[] titles= new String[]{"考试名称","试卷名称","考试人员(真实姓名)","考试人员(用户名)","考试人员(所属分组)","得分","阅卷人"};
				String[] mappingFields= new String[]{"planName","paperName","realName","userName","groupName","totalScore","markUser"};
				List<OmExamSubmitExportVo> exportList = new ArrayList<OmExamSubmitExportVo>();
				for(int i=0;i<list.size();i++){
					OmExamSubmitVo bean = list.get(i);
					OmExamSubmitExportVo dataBean = new OmExamSubmitExportVo();
					BeanUtils.copyProperties(bean,dataBean);
					//获取考试人员所属分组和用户名
					OmUser user = userService.selectByPrimaryKey(bean.getUserId());
					if(user!=null) {
						dataBean.setUserName(user.getUserName());
						dataBean.setGroupName(user.getGroupName());
					}
					exportList.add(dataBean);
				}
				ExcelUtils.writeOneSheet(exportList, titles, mappingFields, "结果信息",response.getOutputStream()); 
			} catch (Exception e) {
				error = e.getMessage();
				throw e;
			} finally {

			}
	}
  	
}
