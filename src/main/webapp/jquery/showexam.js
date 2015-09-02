
function beforeUnload() { return "Nie zapisano zmian, opuścić stronę?";}

var ShowExam =  dejavu.Class.declare({



	initialize : function() {
        this._getJsonData();
        this.startClock();
        this._bindIsChanged();
	},

	pressedSave : function() {
	    var id = "";
	    var $ansDiv = null;
	    var answ = "";
	    var arrayAll = [];
	    var arrayTmp = [];
	    var elems = [];
	    var nr = 0;

        $('section').each(function(){
         id = this.id;
         nr = parseInt(this.getAttribute('name').split('_')[1]);
         elems = document.getElementsByName("quest_" + nr);
         for(var j = 0; j < elems.length; j++) {
           if(elems[j].type == "radio" || elems[j].type == "checkbox") {
                if(elems[j].checked) arrayTmp.push(elems[j].value);
           } else arrayTmp.push(elems[j].value);
         }
         arrayAll.push('{"q": "' + id + '", "a": "' + arrayTmp.join(',;;,') + '", "p": 0 }');
         arrayTmp = [];
        });

        $("#answers").val('[' + arrayAll.join(',') + ']');
        console.log($("#answers").val());
        document.getElementById("answers").onblur();
        window.onbeforeunload = null;
        $('#fixedButton').removeClass('btn-danger').addClass('btn-success');

	},

	_getJsonData : function(){
	    var json = $('#answers').val();
	    console.log("JSON answers: " + json);
	    var data = JSON.parse(json);
	    var qi = {};
	    for(i in data) {
            qi = data[i];
            var $sec = $('#' + qi.q);
            $sec.find('textarea').each(function(){
                this.value = qi.a;
            });
            var a = qi.a.split(',;;,');
            $sec.find('input').each(function(){
               if(this.type == 'text') this.value = qi.a;
               else {
                    for(j in a)
                    if(a[j] == this.value) {this.checked = true; break;}
               }
            });

	    }
	},

	startClock : function() {
        var sec = parseInt($('#secondsToEnd').text().trim());
        if(sec < 0) {
            this.pressedSave();
            location.href = 'http://' + location.host + "/view/exams"
            return;
        }
        $('#secondsToEnd').text(sec - 15);

        $('#timeToEnd').text(Math.floor(sec/3600) + ":" + Math.floor((sec%3600) /60));
        var self = this;
        setTimeout(function() {self.startClock();}, 15000);

	},

	_bindIsChanged : function() {
        $('input').change(function() {
            $('#fixedButton').removeClass('btn-success').addClass('btn-danger');
            window.onbeforeunload = beforeUnload;
        });
         $('textarea').change(function() {
                    $('#fixedButton').removeClass('btn-success').addClass('btn-danger');
                    window.onbeforeunload = beforeUnload;
         });
	}



});