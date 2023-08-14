// https://segmentfault.com/a/1190000015975240
;(function () {
    let customizeType = {
        'material': {
            mounted: function (columnConfig) {
                this.columnConfig = columnConfig

                if (!columnConfig.mode) {
                    columnConfig.mode = 'single'
                }
                
                let _this = this

                this.$dialogInput = $('#dialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (row) {
                        if ("createEvent" in document) {
                            var evt = document.createEvent("HTMLEvents");
                            evt.initEvent("input", false, true);
                            currentMaterialDom.dispatchEvent(evt);
                        }
                        else
                            currentMaterialDom.fireEvent("input");

                        setCaretPosition(currentMaterialDom, currentMaterialDom.value.length)

                        let $tr = $(currentMaterialDom).parent().parent()

                        $editableTable.editableTablePlus('setValue', materialDataMap(row, this), $tr)
                        _this.columnConfig.selected && _this.columnConfig.selected($tr, row)
                    }
                })

                this.$multipleDialogInput = $('#multipleDialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    mode: 'multiple', // 多选
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (rows) {
                        $editableTable.editableTablePlus('appendValue', rows.map(row => materialDataMap(row, this)))

                        _this.columnConfig.selected && _this.columnConfig.selected($(currentMaterialDom).parent().parent(), rows)
                    }
                })

                function materialDataMap(row, context) {
                    return {
                        "materialId": row.materialId ? row.materialId : row.id,
                        "materialCode": row.code,
                        "materialText": context.labelDisplay(row),
                        "materialName": row.name,
                        "materialSpecification": context.specification(row),
                        "unit": row.base_unit,
                        "unitText": row.base_unit_name,
                        "materialCategoryId": row.category_id,
                        "remark": row.remark,
                        "color": row.color,
                    }
                }

                function setCaretPosition(ctrl, pos) {
                    // Modern browsers
                    if (ctrl.setSelectionRange) {
                        ctrl.focus();
                        ctrl.setSelectionRange(pos, pos);
                        // IE8 and below
                    } else if (ctrl.createTextRange) {
                        var range = ctrl.createTextRange();
                        range.collapse(true);
                        range.moveEnd('character', pos);
                        range.moveStart('character', pos);
                        range.select();
                    }
                }
            },
            formatTr: function ($td, columnConfig) {
                $td.find('input')
                    .attr('name', 'materialCode')
                    .attr('readonly', false)

                $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')

                // 注册事件
                let _this = this
                $td.find('input[type=text]').on('click keydown', function(event) {
                    // if ((event.keyCode && event.keyCode === 13) || !event.keyCode) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        currentMaterialDom = this
                        let context = {
                            params: {}
                        }
                        if (!_this.columnConfig.beforeShow || _this.columnConfig.beforeShow($(currentMaterialDom).parent().parent(), context)) {
                            // 获取客户端参数查询
                            if (columnConfig.mode === 'single' || $(this).parents("tr").next().length) {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$dialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$dialogInput.dialogInput().click()
                            } else {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$multipleDialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$multipleDialogInput.dialogInput().click()
                            }
                        }
                    }

                    if (event.keyCode !== 9) {
                        event.preventDefault();
                        return;
                    }
                })
            }
        }
    }


    window.customizeType = customizeType

    window.showMaterialDialog = function (id) {
        $.dialog({
            title: '物料详情',
            content: '/forms/page/695978675677433856/' + id + '?readonly=true',
            class: 'modal-dialog-auto',
            lazy: true,
            showFooter: true
        })
    }
})()



