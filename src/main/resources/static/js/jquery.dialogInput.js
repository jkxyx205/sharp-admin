;var style = document.createElement("style")
style.appendChild(document.createTextNode("@media (min-width: 576px) {.dialog-input .modal-dialog {max-width: 620px!important;} .dialog-input .modal-dialog .modal-body {padding: 0!important;height: 540px;}}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)

;(function($) {
    var DialogInput = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.dialogInput.defaults, options); //合并参数
        this.init();
    };

    DialogInput.prototype = {
        constructor: DialogInput,
        tpl: '<div class="dialog-input modal fade" id="{{id}}" tabindex="-1" role="dialog" aria-labelledby="modalCenterTitle" aria-hidden="true">\n' +
            '    <div class="modal-dialog modal-primary modal-dialog-auto" role="document">\n' +
            '        <div class="modal-content">\n' +
            '            <div class="modal-header">\n' +
            '                <h5 class="modal-title" id="dialog-title"></h5>\n' +
            '                <button class="close" type="button" data-dismiss="modal" aria-label="Close">\n' +
            '                    <span aria-hidden="true">×</span>\n' +
            '                </button>\n' +
            '            </div>\n' +
            '            <div class="modal-body">\n' +
            '                <iframe id="{{iframeId}}" name="{{iframeId}}" src="" width="100%" style="border: 0; padding: 0; margin: 0; height: calc(100% - 6px);"></iframe>\n' +
            '            </div>\n' +
            '            <div class="modal-footer">\n' +
            '                <button class="btn btn-primary ok-show" id="{{okId}}"><i class="fa fa-cog"></i> 确定</button>\n' +
            '                <button class="btn btn-secondary" type="button" data-dismiss="modal"><i class="fa fa-remove"></i> 关闭</button>\n' +
            '            </div>\n' +
            '        </div>\n' +
            '    </div>\n' +
            '</div>',
        init: function() {
            var _this = this
            _this._bindDom()
        },
        _bindDom: function () {
            if (!this.domBind) {
                // bind dialog
                this.modalId = "dialog_id_" + new Date().getTime();
                this.okId = this.modalId + "_okBtn"
                this.iframeId = this.modalId + "_iframeId"

                var dialogTpl = DialogInput.prototype.tpl.replace('{{title}}', this.options.title)
                    .replace('{{id}}', this.modalId)
                    .replace('{{okId}}', this.okId)
                    .replaceAll('{{iframeId}}', this.iframeId)

                $('body').append(dialogTpl)

                this.$modal = $('#' + this.modalId)
                this.$OkBtn = $('#' + this.okId)

                this.$OkBtn.on('click', () => {
                    let row = window.frames[this.iframeId].document.getElementById('qid').row
                    this._dialogRowDbClick(row);
                })

                // bind control
                this.$element.append('<label type="text" class="form-control">'+this.options.placeholder+'</label>\n' +
                    '                    <input type="hidden" name="'+this.options.name+'" class="form-control">')


                this.$element.on('click', () => {
                    this._showReportDialog(this.options.title)
                })

                this.iframe = document.getElementById(this.iframeId)
                this.iframe.input = this.$element.find('input')
                this.iframe.label = this.$element.find('label')

                this.domBind = true
            }
        },
        _showReportDialog:function (title) {
            $('#dialog-title').text(title)

            this.iframe.src = '/reports/' + this.options.reportId

            this.$modal.modal({
                show: true,
                backdrop: 'static'
            })
        },
        _dialogRowDbClick: function (row) {
            if (row && row.id) {
                this.$modal.modal('hide')
                this.iframe.input.val(row.id)
                this.iframe.label.text(this.options.labelDisplay(row))
                this.options.selected && this.options.selected(row)
            } else {
                toastr.error('请先选择一条记录后再点击确定');
            }
        }
    }

    $.fn.dialogInput = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("dialogInput");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("dialogInput", data = new DialogInput(this, options));
                }
            } else {
                if (typeof options == 'string') {
                    if (data[options] instanceof Function) { //调用方法
                        var property = options; [].shift.apply(args);
                        value = data[property].apply(data, args);
                    } else { //获取属性
                        return value = data.options[options];
                    }
                }
            }

        });

        if (value !== undefined) {
            return value;
        } else {
            return chain;
        }

    };

    //设置默认属性
    $.fn.dialogInput.defaults = {
        placeholder: '请选择'
    };

})(jQuery);

function dialogRowDbClick(row) {
    // $('.dialogInput').dialogInput('_dialogRowDbClick', row)
    $('.modal.show .btn.ok-show').click()
}