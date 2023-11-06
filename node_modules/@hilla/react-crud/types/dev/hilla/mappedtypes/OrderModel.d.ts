import { BooleanModel as BooleanModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import DirectionModel_1 from "../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel_1 from "../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
import type Order_1 from "./Order.js";
declare class OrderModel<T extends Order_1 = Order_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => Order_1;
    get direction(): DirectionModel_1;
    get property(): StringModel_1;
    get ignoreCase(): BooleanModel_1;
    get nullHandling(): NullHandlingModel_1;
}
export default OrderModel;
//# sourceMappingURL=OrderModel.d.ts.map