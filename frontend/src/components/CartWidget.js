import "../css/CartWidget.css";
import {useCallback, useContext, useEffect, useState} from "react";
import {UpdatedUserContext, UserPresenceState} from "../contexts/UserContext";
import {AppContext, ServerState} from "../contexts/AppContext";
import Api from "../Api";
import Button from "./Button";

function CartWidget({item}) {

    const appContext = useContext(AppContext);
    const userContext = useContext(UpdatedUserContext);

    const CartEntryState = {
        LOADING: "LOADING",
        PRESENT: "PRESENT",
        EMPTY: "EMPTY"
    }

    const maxQuantity = item["quantity"];

    const [cartEntry, setCartEntry] = useState(null);
    const [cartEntryState, setCartEntryState] = useState(CartEntryState.LOADING);

    const getCartEntryFromCurrentItem = useCallback((cart) => {

        for (let i = 0; i < cart["entries"].length; i++) {
            if(cart["entries"][i]["itemId"] === item["id"]) {
                return cart["entries"][i];
            }
        }
    }, [item]);

    useEffect(() => {

        if(appContext.serverState !== ServerState.AVAILABLE
            || userContext.userPresenceState !== UserPresenceState.PRESENT) {
            return;
        }

        const abortController = new AbortController();

        Api.getCart(abortController.signal)
            .then(retrievedCart => {

                const cartEntry = getCartEntryFromCurrentItem(retrievedCart);

                setCartEntry(cartEntry);

                if(cartEntry) {
                    setCartEntryState(CartEntryState.PRESENT);
                } else {
                    setCartEntryState(CartEntryState.EMPTY);
                }
            });

        return () => abortController.abort();
    }, [appContext, userContext, item, CartEntryState.PRESENT, CartEntryState.EMPTY, getCartEntryFromCurrentItem]);

    const handleAddItemToCartClick = () => {

        Api.incrementItemQuantityInCart(item["id"])
            .then(updatedCart => {
                setCartEntry(getCartEntryFromCurrentItem(updatedCart));
                setCartEntryState(CartEntryState.PRESENT);
            });
    }

    const handleIncrementItemQuantityInCartClick = () => {

        if(cartEntry["quantity"] + 1 > maxQuantity) {
            return;
        }

        Api.incrementItemQuantityInCart(item["id"])
            .then(updatedCart => {
                setCartEntry(getCartEntryFromCurrentItem(updatedCart));
                setCartEntryState(CartEntryState.PRESENT);
            });
    }

    const handleDecrementItemQuantityInCartClick = () => {

        Api.decrementItemQuantityInCart(item["id"])
            .then(updatedCart => {
                setCartEntry(getCartEntryFromCurrentItem(updatedCart));

                if(cartEntry["quantity"] === 1)
                    setCartEntryState(CartEntryState.EMPTY);
            });
    }

    const handleRemoveItemFromCartClick = () => {

        Api.removeItemFromCart(item["id"])
            .then(updatedCart => {
                setCartEntry(getCartEntryFromCurrentItem(updatedCart));
                setCartEntryState(CartEntryState.PRESENT);
            });
    }

    if(appContext.serverState === ServerState.UNDEFINED
        || userContext.userPresenceState === UserPresenceState.LOADING) {
        return null;
    }

    if(appContext.serverState === ServerState.UNAVAILABLE) {
        return <div className="CartWidget">На сервере произошла ошибка при загрузке корзины :(</div>;
    }

    if(maxQuantity === 0) {
        return <div className="CartWidget">Этого товара нет в наличии.</div>;
    }

    if(userContext.userPresenceState === UserPresenceState.EMPTY) {
        return <div className="CartWidget">Войдите в аккаунт, чтобы пользоваться корзиной</div>;
    }

    if(cartEntryState === CartEntryState.LOADING) {
        return null;
    }

    if(cartEntryState === CartEntryState.EMPTY) {
        return (
            <div className="CartWidget">
                <span></span>
                <Button value={"В корзину"} onClick={() => handleAddItemToCartClick()}/>
            </div>
        );
    }

    return (
        <div className="CartWidget">
            <div className="title">В корзине</div>
            <div className="controls">
                <div className="inverter">
                    <div className="cart-quantity-counter">
                        <div className="counter-controls">
                            <span className="noselect"
                                  onClick={() => handleDecrementItemQuantityInCartClick()}>-</span>
                            <span className="noselect disabled">{cartEntry["quantity"]}</span>
                            <span className={"noselect" + (item["quantity"] <= maxQuantity ? " disabled" : "")}
                                  onClick={() => handleIncrementItemQuantityInCartClick()}>
                                +
                            </span>
                        </div>
                        <div className="total-price">
                            {item["price"] * cartEntry["quantity"]}₽
                        </div>
                    </div>
                    {cartEntry["quantity"] >= maxQuantity && <p>Больше не добавить</p>}
                </div>
                <span className="link danger" onClick={() => handleRemoveItemFromCartClick()}>Убрать</span>
            </div>

        </div>
    );
}

export default CartWidget;