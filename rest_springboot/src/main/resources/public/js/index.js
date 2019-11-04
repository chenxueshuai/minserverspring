var modal = {bao:{},company:{},datamap:{},excepList:[]};
var list = modal.datamap;
var company = coverText(modal.company);
var bao = coverText(modal.bao);
var datalist = [];
var app = new Vue({
    el: '#app',
    data: function (){
        return {
            fileparam:{
                type : "1",
                ratioC : "-0.5",
                ratioN1 : "1",
                ratioN2 : "1",
            },
            fileList:[],
            baos:{},
            companys:{},
            value1: "0",
            value2: 'a',
            value3: "0",
            finished: true,
            loading: false,
            option1: bao,
            option3: company,
            option2: [
                { text: '随机排序', value: 'a' },
                { text: '投标金额', value: 'money' },
                { text: '得分', value: 'total' }
            ],
            list:datalist
        }
    },
    methods:{
        submitFile:function () {
            var fd = new FormData();
            fd.append('ratioC', app.fileparam.ratioC);
            fd.append('ratioN1', app.fileparam.ratioN1);
            fd.append('ratioN2', app.fileparam.ratioN2);
            fd.append('file', document.getElementById("file").files[0]);
            $.ajax({
                url:'/tender/upload',
                data: fd ,
                type:'post',
                dataType: 'json',
                processData:false,  //tell jQuery not to process the data
                contentType: false,  //tell jQuery not to set contentType
                success:function(request){
                    if(typeof request == typeof ""){
                        alert(request)
                        return;
                    }
                    modal = request;
                    list = modal.datamap;
                    app.option3 = company = coverText(modal.company);
                    app.option1 = bao = coverText(modal.bao);
                    app.bao = modal.bao;
                    app.companys = modal.company;
                    app.list = datalist = getByBaoId(modal.datamap, bao[0].value);
                }
            });
        },
        baochange:function(value){
            app.list = datalist = getByBaoId(modal.datamap, value);
            app.value3 = '0'
        },
        downloadFile : function () {
            var xhr = new XMLHttpRequest();
            xhr.open('post', '/tender/export', true);
            xhr.responseType = 'blob';
            xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8');
            xhr.onload = function () {
                if (this.status == 200) {
                    var blob = this.response;
                    var a = document.createElement('a');
                    var url = window.URL.createObjectURL(blob);
                    a.href = url;
                    //设置文件名称
                    a.download = '统计结果.xls';
                    a.click();
                }
            }
            xhr.send(JSON.stringify(app.list));
        },
        sortchange:function (value) {
            if("money" == value){
                app.list = app.list.sort(sortByMoney);
            }else {
                app.list = app.list.sort(sortBytotal);
            }

        },
        getBycom:function (value) {
            app.list=splicebyCom(datalist,value)
        },
    }
});
function coverText(data){
    var resArr = [{text:"所有",value:"0"}]
    for(var id in data){
        resArr.push({"text":data[id],"value":id})
    }
    return resArr;
}
function getByBaoId(data,id) {
    if(id == '0'){
        var arr = []
        for(var tid in data){
            arr = arr.concat(data[tid]);
        }
        return arr;
    }else{
        for(var tid in data){
            if(id == tid){
                return data[tid]
            }
        }
    }
    return [];
}
function sortBytotal(b,a){
    return a.total - b.total;
}
function  sortByMoney(b,a){
    return a.money - b.money;
}
function splicebyCom(arr , comV) {
    var res = []
    for (var i = 0; i < arr.length; i ++){
        if(arr[i].name == app.companys[comV]){
            res.push(arr[i])
        }
    }
    return res;
}

