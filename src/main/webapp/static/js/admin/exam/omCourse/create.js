var form = null;
layui.use('form', function(){
	  form = layui.form;
	  form.render();
	  
	//监听提交
	  form.on('submit(formSubmit)', function(data){
		var fntype = $("#fntype").val();  
	    if(isExistName()){
			layer.msg('科目名称已存在，请重新输入', {icon: 5});
			return false;
		}  
		save(data);	
		return false;
	 });
});

function save(data){
	var url = WEBROOT + '/admin/exam/omCourse/save';
	commonSave(data,url);	
}

function isExistName(){
	var courseName = $("#courseName").val();
	var courseId =  $("#courseId").val();
	var flag = true;
	$.ajax({
			type:"POST",
			async:false,
			url:WEBROOT + "/admin/exam/omCourse/isExistName",
			data:{
				courseName:courseName,
				courseId:courseId
			},
			success:function (data) {
				if (data.code == "200") {
					flag = data.data;
				}else{
					layer.msg('查询失败：'+data.message, {icon: 5});
				}
			}
		});
	return flag;
}