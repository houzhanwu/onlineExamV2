package com.plg.shiro.controller.exam;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.plg.shiro.entity.OmExamPlan;
import com.plg.shiro.entity.OmExamPlanVo;
import com.plg.shiro.entity.OmExamUser;
import com.plg.shiro.entity.OmPaper;
import com.plg.shiro.entity.OmUser;
import com.plg.shiro.entity.Vo.MakeUpExamPlanVo;
import com.plg.shiro.service.IExamPlanService;
import com.plg.shiro.service.IExamSubmitService;
import com.plg.shiro.service.IExamUserService;
import com.plg.shiro.service.IPaperService;
import com.plg.shiro.util.DateUtil;
import com.plg.shiro.util.UUIDUtil;
import com.plg.shiro.util.dwz.AjaxObject;
import com.plg.shiro.util.dwz.LayuiPage;

/**
 * 考试安排管理
 *
 */
@Controller
@RequestMapping({ "/admin/exam/omExamPlan" })
public class ExamPlanController {
	private static Logger logger = LoggerFactory.getLogger(ExamPlanController.class);
	
	@Resource
	private IExamPlanService service;
	
	@Resource
	private IPaperService paperService;
	
	@Resource
	private IExamUserService examUserService;
	
	@Resource
	private IExamSubmitService submitService;
	
	private static final String PLAN_PATH = "admin/exam/omExamPlan/";
	
	/**
	 * 考试安排管理列表页
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/list" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String list(HttpServletRequest request) {
		return PLAN_PATH+"list";
	}
	
	/**
	 * 待考列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/examList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String examList(HttpServletRequest request) {
		OmUser omUser = (OmUser) SecurityUtils.getSubject().getSession().getAttribute("om_user");
		List<OmExamPlanVo> planList = service.findUserList(request,omUser,"1");
		request.setAttribute("planList", planList);
		return PLAN_PATH+"examList";
	}
	
	//获取列表数据
  	@RequestMapping("getList")
    @ResponseBody
  	public AjaxObject getList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				List<OmExamPlanVo> list = service.findVoPageList(request, page);
  				result.setData(list);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
  	 //新增修改页面
  	@RequestMapping(value = { "/create" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
  	public String create(HttpServletRequest request) {
  		String planId = request.getParameter("planId");
  		OmExamPlanVo bean = new OmExamPlanVo();
		if(StringUtils.isNotBlank(planId)){
			bean = service.selectVoByPrimaryKey(planId);
    	}
		request.setAttribute("bean", bean);
    	request.setAttribute("fntype", request.getParameter("fntype"));
    	List<OmPaper> paperList = paperService.selectAll();
    	request.setAttribute("paperList", paperList);
    	return PLAN_PATH+"create";
  	}

    //保存修改操作
    @RequestMapping("save")
    @ResponseBody
	public AjaxObject save(HttpServletRequest request,OmExamPlan bean){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			String fntype = request.getParameter("fntype");
			if ("update".equals(fntype)) {
				bean.setUpdateTime(DateUtil.dateParse(new Date(),""));
			    service.updateByPrimaryKeySelective(bean);
			}else{
				bean.setPlanId(UUIDUtil.getUUID());
				bean.setCreateTime(DateUtil.dateParse(new Date(),""));
				//bean.setStatus("1");
				service.insert(bean);
			}
			result.setData(bean);
		} catch (Exception e) {
			logger.error("保存修改考试安排信息："  , e);
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
			logger.error("删除考试安排信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
    /**
	 * 考试人员/评卷人员授权页面
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/assignUser" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String assignUser(HttpServletRequest request) {
		String planId = request.getParameter("planId");
		OmExamPlan bean = new OmExamPlan();
		if(StringUtils.isNotBlank(planId)){
			bean = service.selectByPrimaryKey(planId);
    	}
		request.setAttribute("bean", bean);
		request.setAttribute("examUserType", request.getParameter("examUserType"));
		return PLAN_PATH+"assignUser";
	}
	
	/**
	 * 保存 考试人员/评卷人员授权
	 * @param request
	 * @return
	 */
    @RequestMapping("saveExamUser")
    @ResponseBody
	public AjaxObject saveExamUser(HttpServletRequest request,OmExamUser bean){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			String userIds = request.getParameter("userIds");
			String groupIds = request.getParameter("groupIds");
			examUserService.insertExamUser(bean,userIds,groupIds);
		} catch (Exception e) {
			logger.error("保存考试人员/评卷人员授权信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
    /**
     * 获取授权用户/用户分组记录
     * @param request
     * @return
     */
    @RequestMapping(value = { "/getExamUserList" })
   	@ResponseBody
   	public AjaxObject getExamUserList(HttpServletRequest request){
    	String planId = request.getParameter("planId");
		String examUserType = request.getParameter("examUserType");
    	List<OmExamUser> list = examUserService.selectAll(planId,examUserType);
   		return AjaxObject.newOk("操作成功",list);
   	}
    
    //查询是否可以开始答题
    @RequestMapping("doStartExam")
    @ResponseBody
	public AjaxObject doStartExam(HttpServletRequest request,String paperId,String planId){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			boolean flag = true;
			String message = "";
			OmExamPlan plan =  service.selectByPrimaryKey(planId);
			OmPaper paper = paperService.selectByPrimaryKey(plan.getPaperId());
			//String begin_time = plan.getBeginTime();
			//Date beginDate = DateUtil.string2Date(begin_time, "yyyy-MM-dd HH:mm:ss");
			int paperTime = paper.getPaperTime();
			Calendar nowTime = Calendar.getInstance();
			nowTime.add(Calendar.MINUTE, -paperTime);
			Date nowStartDate = nowTime.getTime();
//			if(beginDate.before(new Date())){//过了开卷时间
//				if(beginDate.before(nowStartDate)){//过了关闭时间
//					flag = false;
//					message = "考试时间已过，考试关闭，稍后移除";
//				}
//			}else{
//				flag = false;
//				message = "开考时间未到，请稍后";
//			}
			result.setData(flag);
			result.setMessage(message);
		} catch (Exception e) {
			logger.error("查询是否可以开始答题："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
    //重新开始练习
    @RequestMapping("reStartTest")
    @ResponseBody
	public AjaxObject reStartTest(HttpServletRequest request,String paperId,String planId){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			OmUser omUser = (OmUser) SecurityUtils.getSubject().getSession().getAttribute("om_user");
			service.clearUserExamAnswer(omUser.getUserId(),planId,paperId);
			result.setMessage("清除数据成功");
		} catch (Exception e) {
			logger.error("查询是否可以开始答题："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
    
    /**
	 * 补考列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/makeupExamList" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String makeupExamList(HttpServletRequest request) {
		return PLAN_PATH+"makeupExamList";
	}

	//获取列表数据
  	@RequestMapping("getMakeupExamList")
    @ResponseBody
  	public AjaxObject getMakeupExamList(HttpServletRequest request,LayuiPage page) throws Exception {
  	  	AjaxObject result = AjaxObject.newOk("success");
  			try {
  				List<OmExamPlanVo> list = service.findVoPageList(request, page);
  				List<MakeUpExamPlanVo> dataList = new ArrayList<MakeUpExamPlanVo>();
  				for(OmExamPlanVo planVo:list) {
  					MakeUpExamPlanVo dataBean = new MakeUpExamPlanVo();
					BeanUtils.copyProperties(planVo,dataBean);
					int passingScore = planVo.getPassingScore();//及格分数
		  			int paperScore = planVo.getPaperScore();//总分数
		  			String passingType = planVo.getPassingType();
		  			if("2".equals(passingType)){//百分比
		  				 passingScore =Math.round((paperScore*passingScore)/100);
		  			}
					//考试人数
					int userNum = submitService.getCountByPlanId(planVo.getPlanId(), planVo.getPaperId(), "4");
					int nopassNum = submitService.getNopassNum(planVo.getPlanId(), planVo.getPaperId(), "4", passingScore);
					int passNum = userNum - nopassNum;
					dataBean.setUserNum(userNum);
					dataBean.setNopassNum(nopassNum);
					dataBean.setPassNum(passNum);
					dataList.add(dataBean);
  				}
  				result.setData(dataList);
  				result.setTotal(page.getTotalCount());
  			} catch(Exception e) {
  				return AjaxObject.newError(e.getMessage());
  			}
  			return result;
  	}
  	
  	/**
	 * 补考安排
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/createMakeupPlan" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String createMakeupPlan(HttpServletRequest request,String paperId,String planId) {
		OmExamPlan plan =  service.selectByPrimaryKey(planId);
		OmExamPlanVo bean =  new OmExamPlanVo();;
		bean.setPlanName(plan.getPlanName()+"_补考");
		request.setAttribute("bean", bean);
		request.setAttribute("fntype", "add");
		request.setAttribute("oldPlanId", planId);
		return PLAN_PATH+"createMakeupPlan";
	}
	
	//获取所有用户
    @RequestMapping(value = { "/getMakeupExamUserList" })
   	@ResponseBody
   	public AjaxObject getMakeupExamUserList(HttpServletRequest request,String planId){
    	List<OmUser> list = submitService.getMakeupExamUserList(planId);
   		return AjaxObject.newOk("操作成功",list);
   	}
    
  //保存修改操作
    @RequestMapping("saveMakeupPlan")
    @ResponseBody
	public AjaxObject saveMakeupPlan(HttpServletRequest request,OmExamPlan bean,OmExamUser userBean){
    	AjaxObject result = AjaxObject.newOk("success");
		try {
			String fntype = request.getParameter("fntype");
			if ("update".equals(fntype)) {
				bean.setUpdateTime(DateUtil.dateParse(new Date(),""));
			    service.updateByPrimaryKeySelective(bean);
			}else{
				bean.setPlanId(UUIDUtil.getUUID());
				bean.setCreateTime(DateUtil.dateParse(new Date(),""));
				//bean.setStatus("1");
				service.insert(bean);
			}
			String userIds = request.getParameter("userIds");
			String groupIds = request.getParameter("groupIds");
			userBean.setPlanId(bean.getPlanId());
			userBean.setPaperId(bean.getPaperId());
			examUserService.insertExamUser(userBean,userIds,groupIds);
			result.setData(bean);
		} catch (Exception e) {
			logger.error("保存修改考试安排信息："  , e);
			return AjaxObject.newError(e.getMessage());
		}
		return result;
	}
  	
   
}